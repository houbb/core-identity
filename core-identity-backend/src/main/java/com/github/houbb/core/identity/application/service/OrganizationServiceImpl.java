package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.*;
import com.github.houbb.core.identity.application.port.*;
import com.github.houbb.core.identity.infrastructure.cache.CaffeineCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Organization management service implementation.
 */
public class OrganizationServiceImpl implements OrganizationService {

    private static final Logger log = LoggerFactory.getLogger(OrganizationServiceImpl.class);

    private final OrganizationRepository orgRepo;
    private final MembershipRepository membershipRepo;
    private final MembershipRoleRepository membershipRoleRepo;
    private final RoleService roleService;
    private final SessionRepository sessionRepo;
    private final AuditService auditService;
    private final OutboxService outboxService;
    private final PasswordHasher passwordHasher;
    private final CredentialRepository credentialRepo;
    private final CaffeineCacheManager cacheManager;

    public OrganizationServiceImpl(OrganizationRepository orgRepo,
                                   MembershipRepository membershipRepo,
                                   MembershipRoleRepository membershipRoleRepo,
                                   RoleService roleService,
                                   SessionRepository sessionRepo,
                                   AuditService auditService,
                                   OutboxService outboxService,
                                   PasswordHasher passwordHasher,
                                   CredentialRepository credentialRepo,
                                   CaffeineCacheManager cacheManager) {
        this.orgRepo = orgRepo;
        this.membershipRepo = membershipRepo;
        this.membershipRoleRepo = membershipRoleRepo;
        this.roleService = roleService;
        this.sessionRepo = sessionRepo;
        this.auditService = auditService;
        this.outboxService = outboxService;
        this.passwordHasher = passwordHasher;
        this.credentialRepo = credentialRepo;
        this.cacheManager = cacheManager;
    }

    @Override
    @Transactional
    public Organization createTeamOrg(String name, String description, String ownerUserId) {
        long now = System.currentTimeMillis();
        String slug = generateSlug(name);
        String orgId = UUID.randomUUID().toString();

        // Create Organization
        Organization org = new Organization();
        org.setId(orgId);
        org.setOrganizationType("TEAM");
        org.setName(name);
        org.setSlug(slug);
        org.setDescription(description);
        org.setOwnerUserId(ownerUserId);
        org.setStatus("ACTIVE");
        org.setAuthorizationVersion(1);
        org.setCreatedAt(now);
        org.setUpdatedAt(now);
        org.setVersion(1);
        orgRepo.save(org);

        // Create Owner Membership
        String membershipId = UUID.randomUUID().toString();
        Membership membership = new Membership();
        membership.setId(membershipId);
        membership.setOrganizationId(orgId);
        membership.setUserId(ownerUserId);
        membership.setMembershipType("OWNER");
        membership.setStatus("ACTIVE");
        membership.setSource("OWNER_CREATED");
        membership.setJoinedAt(now);
        membership.setCreatedBy(ownerUserId);
        membership.setCreatedAt(now);
        membership.setUpdatedAt(now);
        membership.setVersion(1);
        membershipRepo.save(membership);

        // Initialize built-in roles
        List<Role> roles = roleService.initializeBuiltInRoles(orgId, now);

        // Bind OWNER role to membership
        Role ownerRole = roles.stream()
                .filter(r -> "OWNER".equals(r.getRoleKey()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("OWNER role not initialized"));

        MembershipRole mr = new MembershipRole();
        mr.setMembershipId(membershipId);
        mr.setRoleId(ownerRole.getId());
        mr.setAssignedBy(ownerUserId);
        mr.setCreatedAt(now);
        membershipRoleRepo.save(mr);

        // Audit
        writeAudit("ORGANIZATION_CREATED", "USER", ownerUserId, "CREATE", "ORGANIZATION", orgId,
                "SUCCESS", null);

        // Outbox
        writeOutbox("identity.organization.created", "Organization", orgId,
                "{\"organizationId\":\"" + orgId + "\",\"name\":\"" + name + "\",\"ownerUserId\":\"" + ownerUserId + "\"}");

        log.info("Team organization created: {} ({})", name, orgId);
        return org;
    }

    @Override
    @Transactional
    public Organization updateOrganization(String organizationId, String name, String description) {
        Organization org = getOrgOrThrow(organizationId);
        if (!"TEAM".equals(org.getOrganizationType())) {
            throw new ServiceException("IDENTITY_PERSONAL_ORGANIZATION_IMMUTABLE", "个人空间不可修改");
        }

        long now = System.currentTimeMillis();
        if (name != null && !name.trim().isEmpty()) {
            org.setName(name.trim());
        }
        if (description != null) {
            org.setDescription(description);
        }
        org.setUpdatedAt(now);
        orgRepo.update(org);
        return org;
    }

    @Override
    public Organization getOrganization(String organizationId) {
        return getOrgOrThrow(organizationId);
    }

    @Override
    public List<Organization> getMyOrganizations(String userId) {
        return orgRepo.findAllByUserId(userId);
    }

    @Override
    public int getMyOrganizationCount(String userId) {
        return orgRepo.countByUserIdAndStatus(userId, "ACTIVE");
    }

    @Override
    public void switchOrganization(String userId, String organizationId) {
        long now = System.currentTimeMillis();
        // Update membership last accessed
        membershipRepo.findByOrgAndUser(organizationId, userId).ifPresent(mem -> {
            mem.setLastAccessedAt(now);
            membershipRepo.update(mem);
        });

        // Update session preference (best-effort)
        List<Session> sessions = sessionRepo.findByUserIdAndStatus(userId, "ACTIVE");
        for (Session s : sessions) {
            sessionRepo.updateLastOrganizationId(s.getId(), organizationId,
                    s.getPermissionVersion(), now, s.getVersion());
        }
    }

    @Override
    public OrganizationContext getOrganizationContext(String userId, String organizationId) {
        Organization org = getOrgOrThrow(organizationId);

        if (!"ACTIVE".equals(org.getStatus())) {
            throw new ServiceException("IDENTITY_ORGANIZATION_NOT_ACTIVE",
                    "组织当前状态为 " + org.getStatus());
        }

        Membership membership = membershipRepo.findByOrgAndUser(organizationId, userId)
                .orElseThrow(() -> new ServiceException("IDENTITY_MEMBERSHIP_NOT_FOUND", "你不是该组织的成员"));

        if (!"ACTIVE".equals(membership.getStatus())) {
            throw new ServiceException("IDENTITY_MEMBERSHIP_NOT_ACTIVE",
                    "你的成员状态为 " + membership.getStatus());
        }

        List<String> roleIds = membershipRoleRepo.findRoleIdsByMembershipId(membership.getId());

        return new OrganizationContext(organizationId, membership.getId(), membership.getStatus(), roleIds);
    }

    @Override
    @Transactional
    public void transferOwnership(String organizationId, String currentOwnerId,
                                   String newOwnerUserId, String password) {
        Organization org = getOrgOrThrow(organizationId);
        long now = System.currentTimeMillis();

        if (!currentOwnerId.equals(org.getOwnerUserId())) {
            throw new ServiceException("IDENTITY_OWNERSHIP_TRANSFER_INVALID", "只有组织所有者可以转移所有权");
        }

        // Verify password
        Credential cred = credentialRepo.findByUserIdAndType(currentOwnerId, "PASSWORD")
                .orElseThrow(() -> new ServiceException("IDENTITY_INVALID_CREDENTIALS", "认证失败"));
        if (!passwordHasher.matches(password.toCharArray(), cred.getSecretHash())) {
            throw new ServiceException("IDENTITY_INVALID_CREDENTIALS", "密码不正确");
        }

        // Verify target membership
        Membership targetMembership = membershipRepo.findByOrgAndUser(organizationId, newOwnerUserId)
                .orElseThrow(() -> new ServiceException("IDENTITY_MEMBERSHIP_NOT_FOUND", "目标用户不是组织成员"));
        if (!"ACTIVE".equals(targetMembership.getStatus())) {
            throw new ServiceException("IDENTITY_MEMBERSHIP_NOT_ACTIVE", "目标成员状态不可用");
        }

        // Find OWNER role
        Role ownerRole = roleService.getRoles(organizationId).stream()
                .filter(r -> "OWNER".equals(r.getRoleKey()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("OWNER role not found"));

        long newVersion = org.getAuthorizationVersion() + 1;

        // Update ownership
        orgRepo.updateOwner(organizationId, newOwnerUserId, newVersion, now, org.getVersion());

        // Grant OWNER role to new owner
        membershipRoleRepo.save(createMembershipRole(targetMembership.getId(), ownerRole.getId(), currentOwnerId, now));

        // Remove OWNER role from old owner
        Membership oldMembership = membershipRepo.findByOrgAndUser(organizationId, currentOwnerId)
                .orElse(null);
        if (oldMembership != null) {
            membershipRoleRepo.deleteByMembershipAndRole(oldMembership.getId(), ownerRole.getId());
            // Grant ADMIN role to old owner
            roleService.getRoles(organizationId).stream()
                    .filter(r -> "ADMIN".equals(r.getRoleKey()))
                    .findFirst()
                    .ifPresent(adminRole -> {
                        membershipRoleRepo.save(createMembershipRole(oldMembership.getId(),
                                adminRole.getId(), currentOwnerId, now));
                    });
        }

        // Invalidate cache
        cacheManager.invalidateOrganization(organizationId);

        // Audit
        writeAudit("ORGANIZATION_OWNERSHIP_TRANSFERRED", "USER", currentOwnerId, "TRANSFER_OWNERSHIP",
                "ORGANIZATION", organizationId, "SUCCESS",
                "From " + currentOwnerId + " to " + newOwnerUserId);

        writeOutbox("identity.organization.ownership_transferred", "Organization", organizationId,
                "{\"organizationId\":\"" + organizationId + "\",\"fromUserId\":\"" + currentOwnerId +
                "\",\"toUserId\":\"" + newOwnerUserId + "\"}");

        log.info("Ownership transferred for org {}: {} → {}", organizationId, currentOwnerId, newOwnerUserId);
    }

    @Override
    @Transactional
    public void requestDeletion(String organizationId, String userId, String password) {
        Organization org = getOrgOrThrow(organizationId);
        long now = System.currentTimeMillis();

        if (!"TEAM".equals(org.getOrganizationType())) {
            throw new ServiceException("IDENTITY_PERSONAL_ORGANIZATION_IMMUTABLE", "个人空间不可解散");
        }
        if (!userId.equals(org.getOwnerUserId())) {
            throw new ServiceException("IDENTITY_OWNERSHIP_TRANSFER_INVALID", "只有组织所有者可以申请解散");
        }

        // Verify password
        Credential cred = credentialRepo.findByUserIdAndType(userId, "PASSWORD")
                .orElseThrow(() -> new ServiceException("IDENTITY_INVALID_CREDENTIALS", "认证失败"));
        if (!passwordHasher.matches(password.toCharArray(), cred.getSecretHash())) {
            throw new ServiceException("IDENTITY_INVALID_CREDENTIALS", "密码不正确");
        }

        org.setStatus("PENDING_DELETION");
        org.setDeletionRequestedAt(now);
        org.setDeletionEffectiveAt(now + 7 * 24 * 3600 * 1000L); // 7 day cooling
        org.setAuthorizationVersion(org.getAuthorizationVersion() + 1);
        org.setUpdatedAt(now);
        orgRepo.update(org);

        cacheManager.invalidateOrganization(organizationId);

        writeAudit("ORGANIZATION_DELETION_REQUESTED", "USER", userId, "REQUEST_DELETION",
                "ORGANIZATION", organizationId, "SUCCESS", null);

        writeOutbox("identity.organization.deletion_requested", "Organization", organizationId,
                "{\"organizationId\":\"" + organizationId + "\",\"effectiveAt\":" + org.getDeletionEffectiveAt() + "}");
    }

    @Override
    @Transactional
    public void cancelDeletion(String organizationId, String userId) {
        Organization org = getOrgOrThrow(organizationId);

        if (!userId.equals(org.getOwnerUserId())) {
            throw new ServiceException("IDENTITY_OWNERSHIP_TRANSFER_INVALID", "只有组织所有者可以取消解散");
        }
        if (!"PENDING_DELETION".equals(org.getStatus())) {
            throw new ServiceException("IDENTITY_ORGANIZATION_NOT_ACTIVE", "组织当前不处于待删除状态");
        }

        long now = System.currentTimeMillis();
        org.setStatus("ACTIVE");
        org.setDeletionRequestedAt(null);
        org.setDeletionEffectiveAt(null);
        org.setUpdatedAt(now);
        orgRepo.update(org);
    }

    @Override
    @Transactional
    public void suspendOrganization(String organizationId, String reason, String operatorId) {
        Organization org = getOrgOrThrow(organizationId);
        long now = System.currentTimeMillis();

        if ("PERSONAL".equals(org.getOrganizationType())) {
            throw new ServiceException("IDENTITY_PERSONAL_ORGANIZATION_IMMUTABLE", "个人空间不可冻结");
        }

        org.setStatus("SUSPENDED");
        org.setSuspendedAt(now);
        org.setSuspendedReason(reason);
        org.setAuthorizationVersion(org.getAuthorizationVersion() + 1);
        org.setUpdatedAt(now);
        orgRepo.update(org);

        cacheManager.invalidateOrganization(organizationId);

        writeAudit("ORGANIZATION_SUSPENDED", "SYSTEM", operatorId, "SUSPEND",
                "ORGANIZATION", organizationId, "SUCCESS", reason);

        writeOutbox("identity.organization.suspended", "Organization", organizationId,
                "{\"organizationId\":\"" + organizationId + "\",\"reason\":\"" + reason + "\"}");
    }

    @Override
    @Transactional
    public void reactivateOrganization(String organizationId, String operatorId) {
        Organization org = getOrgOrThrow(organizationId);

        if (!"SUSPENDED".equals(org.getStatus())) {
            throw new ServiceException("IDENTITY_ORGANIZATION_NOT_ACTIVE", "组织当前不是冻结状态");
        }

        long now = System.currentTimeMillis();
        org.setStatus("ACTIVE");
        org.setSuspendedAt(null);
        org.setSuspendedReason(null);
        org.setUpdatedAt(now);
        orgRepo.update(org);

        cacheManager.invalidateOrganization(organizationId);

        writeAudit("ORGANIZATION_REACTIVATED", "SYSTEM", operatorId, "REACTIVATE",
                "ORGANIZATION", organizationId, "SUCCESS", null);

        writeOutbox("identity.organization.reactivated", "Organization", organizationId,
                "{\"organizationId\":\"" + organizationId + "\"}");
    }

    @Override
    public String generateSlug(String name) {
        // Remove special characters, convert to lowercase, truncate
        String slug = name.toLowerCase()
                .replaceAll("[^a-z0-9\\u4e00-\\u9fff]+", "-")
                .replaceAll("^-|-$", "")
                .replaceAll("-{2,}", "-");

        if (slug.isEmpty()) {
            slug = "org-" + System.currentTimeMillis() % 100000;
        }
        if (slug.length() > 80) {
            slug = slug.substring(0, 80);
        }

        // Check uniqueness and append suffix if needed
        String baseSlug = slug;
        int suffix = 2;
        while (orgRepo.findBySlug(slug).isPresent()) {
            slug = baseSlug + "-" + suffix;
            suffix++;
            if (slug.length() > 100) {
                slug = baseSlug.substring(0, 90) + "-" + suffix;
            }
        }

        return slug;
    }

    private Organization getOrgOrThrow(String organizationId) {
        return orgRepo.findById(organizationId)
                .orElseThrow(() -> new ServiceException("IDENTITY_ORGANIZATION_NOT_FOUND", "组织不存在"));
    }

    private MembershipRole createMembershipRole(String membershipId, String roleId, String assignedBy, long now) {
        MembershipRole mr = new MembershipRole();
        mr.setMembershipId(membershipId);
        mr.setRoleId(roleId);
        mr.setAssignedBy(assignedBy);
        mr.setCreatedAt(now);
        return mr;
    }

    private void writeAudit(String eventType, String actorType, String actorId, String action,
                            String targetType, String targetId, String result, String reason) {
        try {
            com.github.houbb.core.identity.application.command.AuditCommand cmd =
                    new com.github.houbb.core.identity.application.command.AuditCommand();
            cmd.setEventType(eventType);
            cmd.setActorType(actorType);
            cmd.setActorId(actorId);
            cmd.setAction(action);
            cmd.setTargetType(targetType);
            cmd.setTargetId(targetId);
            cmd.setResult(result);
            cmd.setReason(reason);
            auditService.record(cmd);
        } catch (Exception e) {
            log.warn("Failed to write audit: {}", e.getMessage());
        }
    }

    private void writeOutbox(String eventType, String aggregateType, String aggregateId, String payloadJson) {
        try {
            com.github.houbb.core.identity.application.command.OutboxCommand cmd =
                    new com.github.houbb.core.identity.application.command.OutboxCommand();
            cmd.setEventType(eventType);
            cmd.setAggregateType(aggregateType);
            cmd.setAggregateId(aggregateId);
            cmd.setPayloadJson(payloadJson);
            outboxService.write(cmd);
        } catch (Exception e) {
            log.warn("Failed to write outbox: {}", e.getMessage());
        }
    }

    public static class ServiceException extends RuntimeException {
        private final String errorCode;

        public ServiceException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }
}