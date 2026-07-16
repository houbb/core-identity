package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.AccessGrant;
import com.github.houbb.core.identity.application.domain.Entitlement;
import com.github.houbb.core.identity.application.domain.Membership;
import com.github.houbb.core.identity.application.domain.MembershipRole;
import com.github.houbb.core.identity.application.domain.Organization;
import com.github.houbb.core.identity.application.domain.Role;
import com.github.houbb.core.identity.application.port.AccessGrantRepository;
import com.github.houbb.core.identity.application.port.EntitlementRepository;
import com.github.houbb.core.identity.application.port.MembershipRepository;
import com.github.houbb.core.identity.application.port.MembershipRoleRepository;
import com.github.houbb.core.identity.application.port.OrganizationRepository;
import com.github.houbb.core.identity.application.port.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public class GrantServiceImpl implements GrantService {

    private static final Logger log = LoggerFactory.getLogger(GrantServiceImpl.class);

    private final AccessGrantRepository grantRepo;
    private final EntitlementRepository entitlementRepo;
    private final MembershipRoleRepository membershipRoleRepo;
    private final MembershipRepository membershipRepo;
    private final RoleRepository roleRepo;
    private final OrganizationRepository orgRepo;

    public GrantServiceImpl(AccessGrantRepository grantRepo,
                            EntitlementRepository entitlementRepo,
                            MembershipRoleRepository membershipRoleRepo,
                            MembershipRepository membershipRepo,
                            RoleRepository roleRepo,
                            OrganizationRepository orgRepo) {
        this.grantRepo = grantRepo;
        this.entitlementRepo = entitlementRepo;
        this.membershipRoleRepo = membershipRoleRepo;
        this.membershipRepo = membershipRepo;
        this.roleRepo = roleRepo;
        this.orgRepo = orgRepo;
    }

    @Override
    @Transactional
    public AccessGrant createGrant(String subjectType, String subjectId, String organizationId,
                                    String entitlementId, String sourceType, String sourceId,
                                    String grantType, long validFrom, long expiresAt, String grantedBy) {
        long now = System.currentTimeMillis();

        // 验证 Entitlement 存在且活跃
        Entitlement entitlement = entitlementRepo.findById(entitlementId)
                .orElseThrow(() -> new ServiceException("IDENTITY_ENTITLEMENT_NOT_FOUND",
                        "权益 " + entitlementId + " 不存在"));
        if (!"ACTIVE".equals(entitlement.getStatus())) {
            throw new ServiceException("IDENTITY_ENTITLEMENT_NOT_ACTIVE",
                    "权益 " + entitlement.getCode() + " 未激活");
        }

        // 如果是 ROLE 类型的 Entitlement，且主体是 USER，应用角色到成员关系
        if ("USER".equals(subjectType) && "ROLE".equals(entitlement.getEntitlementType()) && organizationId != null) {
            applyRoleToMembership(subjectId, organizationId, entitlement.getTargetId());
        }

        AccessGrant grant = new AccessGrant();
        grant.setId(UUID.randomUUID().toString());
        grant.setSubjectType(subjectType);
        grant.setSubjectId(subjectId);
        grant.setOrganizationId(organizationId);
        grant.setEntitlementId(entitlementId);
        grant.setSourceType(sourceType);
        grant.setSourceId(sourceId);
        grant.setGrantType(grantType != null ? grantType : "STANDARD");
        grant.setStatus("ACTIVE");
        grant.setValidFrom(validFrom > 0 ? validFrom : now);
        grant.setExpiresAt(expiresAt);
        grant.setGrantedBy(grantedBy);
        grant.setCreatedAt(now);
        grant.setUpdatedAt(now);
        grant.setVersion(1);

        grantRepo.save(grant);
        log.info("Created grant: id={}, subject={}/{}, entitlement={}, source={}/{}",
                grant.getId(), subjectType, subjectId, entitlement.getCode(), sourceType, sourceId);
        return grant;
    }

    @Override
    public List<AccessGrant> listActiveBySubject(String subjectId) {
        return grantRepo.findActiveBySubjectId(subjectId);
    }

    @Override
    public List<AccessGrant> listBySubjectAndOrg(String subjectId, String organizationId) {
        return grantRepo.findBySubjectIdAndOrg(subjectId, organizationId);
    }

    @Override
    public AccessGrant getById(String id) {
        return grantRepo.findById(id)
                .orElseThrow(() -> new ServiceException("IDENTITY_GRANT_NOT_FOUND",
                        "授权 " + id + " 不存在"));
    }

    @Override
    @Transactional
    public AccessGrant renew(String id, long newExpiresAt, String operatorId) {
        AccessGrant grant = getById(id);
        if (!"ACTIVE".equals(grant.getStatus())) {
            throw new ServiceException("IDENTITY_GRANT_NOT_ACTIVE",
                    "授权 " + id + " 不是活跃状态，无法续期");
        }
        long now = System.currentTimeMillis();
        grantRepo.updateStatusAndExpiry(id, grant.getStatus(), newExpiresAt, now, grant.getVersion());
        log.info("Renewed grant: id={}, newExpiresAt={}, by={}", id, newExpiresAt, operatorId);
        return getById(id);
    }

    @Override
    @Transactional
    public void revoke(String id, String revokedBy, String reason) {
        AccessGrant grant = getById(id);
        if ("REVOKED".equals(grant.getStatus()) || "EXPIRED".equals(grant.getStatus())) {
            return;
        }
        long now = System.currentTimeMillis();

        // 如果 Grant 关联了 ROLE，从 Membership 中移除
        if ("USER".equals(grant.getSubjectType()) && grant.getOrganizationId() != null) {
            Entitlement entitlement = entitlementRepo.findById(grant.getEntitlementId()).orElse(null);
            if (entitlement != null && "ROLE".equals(entitlement.getEntitlementType())) {
                revokeRoleFromMembership(grant.getSubjectId(), grant.getOrganizationId(), entitlement.getTargetId());
            }
        }

        grantRepo.revoke(id, revokedBy, now, reason, now, grant.getVersion());
        log.info("Revoked grant: id={}, by={}, reason={}", id, revokedBy, reason);
    }

    @Override
    @Transactional
    public void markExpired(String id) {
        AccessGrant grant = getById(id);
        if ("EXPIRED".equals(grant.getStatus())) {
            return;
        }
        long now = System.currentTimeMillis();
        grantRepo.updateStatus(id, "EXPIRED", now, grant.getVersion());
        log.info("Grant expired: id={}", id);
    }

    @Override
    public List<AccessGrant> findExpiringGrants(long beforeTimestamp) {
        return grantRepo.findExpiringGrants(beforeTimestamp, "ACTIVE");
    }

    @Override
    @Transactional
    public void processExpiredGrant(String grantId) {
        AccessGrant grant = getById(grantId);
        if (!"ACTIVE".equals(grant.getStatus())) {
            return;
        }

        long now = System.currentTimeMillis();

        // 撤销关联的角色
        if ("USER".equals(grant.getSubjectType()) && grant.getOrganizationId() != null) {
            Entitlement entitlement = entitlementRepo.findById(grant.getEntitlementId()).orElse(null);
            if (entitlement != null && "ROLE".equals(entitlement.getEntitlementType())) {
                revokeRoleFromMembership(grant.getSubjectId(), grant.getOrganizationId(), entitlement.getTargetId());
            }
        }

        // 标记 Grant 为已过期
        grantRepo.updateStatus(grantId, "EXPIRED", now, grant.getVersion());

        // 增加组织的 authorization_version（通过 update 实现）
        if (grant.getOrganizationId() != null) {
            orgRepo.findById(grant.getOrganizationId()).ifPresent(org -> {
                org.setUpdatedAt(now);
                orgRepo.update(org);
            });
        }

        log.info("Processed expired grant: id={}, subject={}, entitlement={}",
                grantId, grant.getSubjectId(), grant.getEntitlementId());
    }

    @Override
    @Transactional
    public void recordUsage(String grantId) {
        AccessGrant grant = getById(grantId);
        if (!"ACTIVE".equals(grant.getStatus())) {
            return;
        }
        grant.setLastUsedAt(System.currentTimeMillis());
        grant.setUpdatedAt(System.currentTimeMillis());
        grantRepo.update(grant);
    }

    @Override
    public List<AccessGrant> listBySource(String sourceType, String sourceId) {
        return grantRepo.findBySourceTypeAndSourceId(sourceType, sourceId);
    }

    /**
     * 将角色应用到成员的 Membership 上。
     */
    private void applyRoleToMembership(String userId, String organizationId, String roleId) {
        Membership membership = membershipRepo.findByOrgAndUser(organizationId, userId)
                .orElseThrow(() -> new ServiceException("IDENTITY_MEMBERSHIP_NOT_FOUND",
                        "用户不是该组织的成员"));

        // 检查角色是否已分配
        List<MembershipRole> existingRoles = membershipRoleRepo.findByMembershipId(membership.getId());
        boolean alreadyHas = existingRoles.stream()
                .anyMatch(mr -> mr.getRoleId().equals(roleId));
        if (alreadyHas) {
            return;
        }

        MembershipRole mr = new MembershipRole();
        mr.setMembershipId(membership.getId());
        mr.setRoleId(roleId);
        mr.setAssignedBy("governance");
        mr.setCreatedAt(System.currentTimeMillis());
        membershipRoleRepo.save(mr);

        log.info("Applied role {} to user {} in org {}", roleId, userId, organizationId);
    }

    /**
     * 从成员的 Membership 中移除角色。
     */
    private void revokeRoleFromMembership(String userId, String organizationId, String roleId) {
        membershipRepo.findByOrgAndUser(organizationId, userId).ifPresent(membership -> {
            List<MembershipRole> existingRoles = membershipRoleRepo.findByMembershipId(membership.getId());
            for (MembershipRole mr : existingRoles) {
                if (mr.getRoleId().equals(roleId)) {
                    membershipRoleRepo.deleteByMembershipAndRole(membership.getId(), roleId);
                    log.info("Revoked role {} from user {} in org {}", roleId, userId, organizationId);
                    return;
                }
            }
        });
    }

    public static class ServiceException extends RuntimeException {
        private final String errorCode;

        public ServiceException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }

        public String getErrorCode() { return errorCode; }
    }
}