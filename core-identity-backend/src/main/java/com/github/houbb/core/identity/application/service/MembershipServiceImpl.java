package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.*;
import com.github.houbb.core.identity.application.port.*;
import com.github.houbb.core.identity.infrastructure.cache.CaffeineCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

public class MembershipServiceImpl implements MembershipService {

    private static final Logger log = LoggerFactory.getLogger(MembershipServiceImpl.class);

    private final MembershipRepository membershipRepo;
    private final MembershipRoleRepository membershipRoleRepo;
    private final OrganizationRepository orgRepo;
    private final RoleRepository roleRepo;
    private final UserRepository userRepo;
    private final UserEmailRepository emailRepo;
    private final AuditService auditService;
    private final OutboxService outboxService;
    private final CaffeineCacheManager cacheManager;

    public MembershipServiceImpl(MembershipRepository membershipRepo,
                                  MembershipRoleRepository membershipRoleRepo,
                                  OrganizationRepository orgRepo,
                                  RoleRepository roleRepo,
                                  UserRepository userRepo,
                                  UserEmailRepository emailRepo,
                                  AuditService auditService,
                                  OutboxService outboxService,
                                  CaffeineCacheManager cacheManager) {
        this.membershipRepo = membershipRepo;
        this.membershipRoleRepo = membershipRoleRepo;
        this.orgRepo = orgRepo;
        this.roleRepo = roleRepo;
        this.userRepo = userRepo;
        this.emailRepo = emailRepo;
        this.auditService = auditService;
        this.outboxService = outboxService;
        this.cacheManager = cacheManager;
    }

    @Override
    public List<MembershipDTO> getMembers(String organizationId, String status, String role, String search) {
        List<Membership> memberships;
        if (status != null && !status.isEmpty()) {
            memberships = membershipRepo.findByOrgAndStatus(organizationId, status);
        } else {
            memberships = membershipRepo.findByOrganizationId(organizationId);
        }

        // Collect all membership IDs for batch role lookup
        List<String> membershipIds = memberships.stream().map(Membership::getId).toList();
        List<String> allAssignedRoleIds = membershipRoleRepo.findRoleIdsByMembershipIds(membershipIds);
        List<Role> allRoles = roleRepo.findByOrgId(organizationId);
        Map<String, Role> roleMap = new HashMap<>();
        for (Role r : allRoles) roleMap.put(r.getId(), r);

        List<MembershipDTO> result = new ArrayList<>();
        for (Membership m : memberships) {
            List<String> roleIds = membershipRoleRepo.findRoleIdsByMembershipId(m.getId());
            List<String> roleNames = roleIds.stream()
                    .map(roleMap::get)
                    .filter(Objects::nonNull)
                    .map(Role::getName)
                    .toList();

            User user = userRepo.findById(m.getUserId()).orElse(null);
            String displayName = user != null ? user.getDisplayName() : "Unknown";
            String email = emailRepo.findByUserId(m.getUserId())
                    .map(UserEmail::getEmailDisplay).orElse("");

            // Filter by search
            if (search != null && !search.isEmpty() &&
                    !displayName.toLowerCase().contains(search.toLowerCase()) &&
                    !email.toLowerCase().contains(search.toLowerCase())) {
                continue;
            }

            // Filter by role
            if (role != null && !role.isEmpty() && !roleNames.contains(role)) {
                continue;
            }

            result.add(new MembershipDTO(
                    m.getId(), m.getOrganizationId(), m.getUserId(),
                    displayName, email, m.getStatus(), m.getSource(),
                    roleNames, roleIds, m.getJoinedAt(), m.getLastAccessedAt(), m.getCreatedAt()
            ));
        }

        return result;
    }

    @Override
    public MembershipDTO getMember(String organizationId, String membershipId) {
        Membership m = membershipRepo.findById(membershipId)
                .orElseThrow(() -> new ServiceException("IDENTITY_MEMBERSHIP_NOT_FOUND", "成员不存在"));
        if (!m.getOrganizationId().equals(organizationId)) {
            throw new ServiceException("IDENTITY_MEMBERSHIP_NOT_FOUND", "成员不属于该组织");
        }

        List<String> roleIds = membershipRoleRepo.findRoleIdsByMembershipId(m.getId());
        List<Role> roles = roleRepo.findByIds(roleIds);

        User user = userRepo.findById(m.getUserId()).orElse(null);
        String displayName = user != null ? user.getDisplayName() : "Unknown";
        String email = emailRepo.findByUserId(m.getUserId()).map(UserEmail::getEmailDisplay).orElse("");

        return new MembershipDTO(m.getId(), m.getOrganizationId(), m.getUserId(),
                displayName, email, m.getStatus(), m.getSource(),
                roles.stream().map(Role::getName).toList(),
                roleIds, m.getJoinedAt(), m.getLastAccessedAt(), m.getCreatedAt());
    }

    @Override
    @Transactional
    public void updateMemberRoles(String organizationId, String membershipId, List<String> roleIds, String operatorId) {
        long now = System.currentTimeMillis();
        Membership m = getOrgMembership(organizationId, membershipId);

        Organization org = orgRepo.findById(organizationId)
                .orElseThrow(() -> new ServiceException("IDENTITY_ORGANIZATION_NOT_FOUND", "组织不存在"));

        // Verify not modifying owner directly through roles
        if (m.getUserId().equals(org.getOwnerUserId())) {
            // Owner must keep OWNER role
            boolean hasOwner = roleIds.stream().anyMatch(rid -> {
                Role r = roleRepo.findById(rid).orElse(null);
                return r != null && "OWNER".equals(r.getRoleKey());
            });
            if (!hasOwner) {
                throw new ServiceException("IDENTITY_OWNER_CANNOT_BE_REMOVED", "不能移除所有者的 OWNER 角色");
            }
        }

        // Verify all roles belong to the same organization
        for (String roleId : roleIds) {
            Role role = roleRepo.findById(roleId)
                    .orElseThrow(() -> new ServiceException("IDENTITY_ROLE_NOT_FOUND", "角色 " + roleId + " 不存在"));
            if (!role.getOrganizationId().equals(organizationId)) {
                throw new ServiceException("IDENTITY_ROLE_ORGANIZATION_MISMATCH", "角色不属于当前组织");
            }
        }

        if (roleIds.isEmpty()) {
            throw new ServiceException("IDENTITY_MEMBER_ROLE_REQUIRED", "成员至少需要一个角色");
        }

        membershipRoleRepo.replaceRoles(membershipId, roleIds, operatorId, now);

        // Increment authorization version
        org.setAuthorizationVersion(org.getAuthorizationVersion() + 1);
        org.setUpdatedAt(now);
        orgRepo.update(org);

        cacheManager.invalidate(m.getUserId(), organizationId);

        writeAudit("MEMBER_ROLES_CHANGED", "USER", operatorId, "CHANGE_ROLES",
                "MEMBERSHIP", membershipId, "SUCCESS",
                "Assigned roles: " + String.join(",", roleIds));
        writeOutbox("identity.membership.roles_changed", "Membership", membershipId,
                "{\"membershipId\":\"" + membershipId + "\",\"userId\":\"" + m.getUserId() +
                "\",\"organizationId\":\"" + organizationId + "\"}");
    }

    @Override
    @Transactional
    public void suspendMember(String organizationId, String membershipId, String operatorId) {
        long now = System.currentTimeMillis();
        Membership m = getOrgMembership(organizationId, membershipId);

        Organization org = orgRepo.findById(organizationId).orElseThrow();
        if (m.getUserId().equals(org.getOwnerUserId())) {
            throw new ServiceException("IDENTITY_OWNER_CANNOT_BE_REMOVED", "不能暂停组织所有者");
        }

        m.setStatus("SUSPENDED");
        m.setSuspendedAt(now);
        m.setUpdatedAt(now);
        membershipRepo.update(m);

        cacheManager.invalidate(m.getUserId(), organizationId);

        writeAudit("MEMBER_SUSPENDED", "USER", operatorId, "SUSPEND_MEMBER",
                "MEMBERSHIP", membershipId, "SUCCESS", null);
        writeOutbox("identity.membership.suspended", "Membership", membershipId,
                "{\"membershipId\":\"" + membershipId + "\",\"userId\":\"" + m.getUserId() + "\"}");
    }

    @Override
    @Transactional
    public void reactivateMember(String organizationId, String membershipId, String operatorId) {
        long now = System.currentTimeMillis();
        Membership m = getOrgMembership(organizationId, membershipId);

        m.setStatus("ACTIVE");
        m.setSuspendedAt(null);
        m.setUpdatedAt(now);
        membershipRepo.update(m);

        writeAudit("MEMBER_REACTIVATED", "USER", operatorId, "REACTIVATE_MEMBER",
                "MEMBERSHIP", membershipId, "SUCCESS", null);
        writeOutbox("identity.membership.reactivated", "Membership", membershipId,
                "{\"membershipId\":\"" + membershipId + "\",\"userId\":\"" + m.getUserId() + "\"}");
    }

    @Override
    @Transactional
    public void removeMember(String organizationId, String membershipId, String operatorId) {
        long now = System.currentTimeMillis();
        Membership m = getOrgMembership(organizationId, membershipId);

        Organization org = orgRepo.findById(organizationId).orElseThrow();
        if (m.getUserId().equals(org.getOwnerUserId())) {
            throw new ServiceException("IDENTITY_OWNER_CANNOT_BE_REMOVED", "不能移除组织所有者");
        }

        m.setStatus("REMOVED");
        m.setRemovedAt(now);
        m.setUpdatedAt(now);
        membershipRepo.update(m);

        // Clean up role bindings
        membershipRoleRepo.deleteAllByMembershipId(membershipId);

        cacheManager.invalidate(m.getUserId(), organizationId);

        writeAudit("MEMBER_REMOVED", "USER", operatorId, "REMOVE_MEMBER",
                "MEMBERSHIP", membershipId, "SUCCESS", null);
        writeOutbox("identity.membership.removed", "Membership", membershipId,
                "{\"membershipId\":\"" + membershipId + "\",\"userId\":\"" + m.getUserId() +
                "\",\"organizationId\":\"" + organizationId + "\"}");
    }

    @Override
    @Transactional
    public void leaveOrganization(String organizationId, String userId) {
        long now = System.currentTimeMillis();
        Membership m = membershipRepo.findByOrgAndUser(organizationId, userId)
                .orElseThrow(() -> new ServiceException("IDENTITY_MEMBERSHIP_NOT_FOUND", "你不是该组织成员"));

        Organization org = orgRepo.findById(organizationId).orElseThrow();
        if (userId.equals(org.getOwnerUserId())) {
            throw new ServiceException("IDENTITY_OWNER_CANNOT_LEAVE",
                    "你是组织所有者，请先转移所有权后再退出");
        }

        m.setStatus("LEFT");
        m.setLeftAt(now);
        m.setUpdatedAt(now);
        membershipRepo.update(m);

        membershipRoleRepo.deleteAllByMembershipId(m.getId());

        cacheManager.invalidate(userId, organizationId);

        writeAudit("MEMBER_LEFT", "USER", userId, "LEAVE_ORGANIZATION",
                "MEMBERSHIP", m.getId(), "SUCCESS", null);
        writeOutbox("identity.membership.left", "Membership", m.getId(),
                "{\"membershipId\":\"" + m.getId() + "\",\"userId\":\"" + userId +
                "\",\"organizationId\":\"" + organizationId + "\"}");
    }

    private Membership getOrgMembership(String organizationId, String membershipId) {
        Membership m = membershipRepo.findById(membershipId)
                .orElseThrow(() -> new ServiceException("IDENTITY_MEMBERSHIP_NOT_FOUND", "成员不存在"));
        if (!m.getOrganizationId().equals(organizationId)) {
            throw new ServiceException("IDENTITY_MEMBERSHIP_NOT_FOUND", "成员不属于该组织");
        }
        return m;
    }

    private void writeAudit(String eventType, String actorType, String actorId, String action,
                            String targetType, String targetId, String result, String reason) {
        try {
            var cmd = new com.github.houbb.core.identity.application.command.AuditCommand();
            cmd.setEventType(eventType); cmd.setActorType(actorType); cmd.setActorId(actorId);
            cmd.setAction(action); cmd.setTargetType(targetType); cmd.setTargetId(targetId);
            cmd.setResult(result); cmd.setReason(reason);
            auditService.record(cmd);
        } catch (Exception e) { log.warn("Failed to write audit: {}", e.getMessage()); }
    }

    private void writeOutbox(String eventType, String aggregateType, String aggregateId, String payloadJson) {
        try {
            var cmd = new com.github.houbb.core.identity.application.command.OutboxCommand();
            cmd.setEventType(eventType); cmd.setAggregateType(aggregateType);
            cmd.setAggregateId(aggregateId); cmd.setPayloadJson(payloadJson);
            outboxService.write(cmd);
        } catch (Exception e) { log.warn("Failed to write outbox: {}", e.getMessage()); }
    }

    public static class ServiceException extends RuntimeException {
        private final String errorCode;
        public ServiceException(String errorCode, String message) { super(message); this.errorCode = errorCode; }
        public String getErrorCode() { return errorCode; }
    }
}