package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.Organization;
import com.github.houbb.core.identity.application.domain.Membership;
import com.github.houbb.core.identity.application.port.*;
import com.github.houbb.core.identity.application.service.AuthorizationService.*;
import com.github.houbb.core.identity.infrastructure.cache.CaffeineCacheManager;
import com.github.houbb.core.identity.infrastructure.cache.CaffeineCacheManager.CachedPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AuthorizationServiceImpl implements AuthorizationService {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationServiceImpl.class);

    private final OrganizationRepository orgRepo;
    private final MembershipRepository membershipRepo;
    private final MembershipRoleRepository membershipRoleRepo;
    private final RolePermissionRepository rolePermissionRepo;
    private final RoleRepository roleRepo;
    private final CaffeineCacheManager cacheManager;

    public AuthorizationServiceImpl(OrganizationRepository orgRepo,
                                     MembershipRepository membershipRepo,
                                     MembershipRoleRepository membershipRoleRepo,
                                     RolePermissionRepository rolePermissionRepo,
                                     RoleRepository roleRepo,
                                     CaffeineCacheManager cacheManager) {
        this.orgRepo = orgRepo;
        this.membershipRepo = membershipRepo;
        this.membershipRoleRepo = membershipRoleRepo;
        this.rolePermissionRepo = rolePermissionRepo;
        this.roleRepo = roleRepo;
        this.cacheManager = cacheManager;
    }

    @Override
    public AuthorizationResult check(String userId, String organizationId, String permissionCode) {
        Organization org = orgRepo.findById(organizationId).orElse(null);
        if (org == null) {
            return AuthorizationResult.DENY_ORGANIZATION_INACTIVE;
        }

        // SUSPENDED org: allow reads, deny writes
        boolean isReadOperation = permissionCode.endsWith(".read");
        if ("SUSPENDED".equals(org.getStatus()) && !isReadOperation) {
            return AuthorizationResult.DENY_ORGANIZATION_INACTIVE;
        }
        if ("DELETED".equals(org.getStatus()) || "PENDING_DELETION".equals(org.getStatus())) {
            return AuthorizationResult.DENY_ORGANIZATION_INACTIVE;
        }

        Membership membership = membershipRepo.findByOrgAndUser(organizationId, userId).orElse(null);
        if (membership == null) {
            return AuthorizationResult.DENY_MEMBERSHIP_NOT_FOUND;
        }
        if (!"ACTIVE".equals(membership.getStatus())) {
            return AuthorizationResult.DENY_MEMBERSHIP_INACTIVE;
        }

        Set<String> permissionCodes = getEffectivePermissions(userId, organizationId);
        if (permissionCodes.contains(permissionCode)) {
            return AuthorizationResult.ALLOW;
        }

        return AuthorizationResult.DENY_PERMISSION_MISSING;
    }

    @Override
    public void require(String userId, String organizationId, String permissionCode) {
        AuthorizationResult result = check(userId, organizationId, permissionCode);
        if (result != AuthorizationResult.ALLOW) {
            throw new RoleServiceImpl.ServiceException("IDENTITY_PERMISSION_DENIED",
                    "缺少权限: " + permissionCode);
        }
    }

    @Override
    public Set<String> getEffectivePermissions(String userId, String organizationId) {
        // Check cache
        CachedPermissions cached = cacheManager.get(userId, organizationId);
        Organization org = orgRepo.findById(organizationId).orElse(null);
        if (org != null && cached != null && cached.authorizationVersion() == org.getAuthorizationVersion()) {
            return cached.permissionCodes();
        }

        // Compute from DB
        Membership membership = membershipRepo.findByOrgAndUser(organizationId, userId).orElse(null);
        if (membership == null) {
            return Collections.emptySet();
        }

        List<String> roleIds = membershipRoleRepo.findRoleIdsByMembershipId(membership.getId());
        if (roleIds.isEmpty()) {
            return Collections.emptySet();
        }

        List<String> permissionCodes = rolePermissionRepo.findPermissionCodesByRoleIds(roleIds);
        Set<String> result = new HashSet<>(permissionCodes);

        // Cache
        if (org != null) {
            cacheManager.put(userId, organizationId, new HashSet<>(roleIds), result, org.getAuthorizationVersion());
        }

        return result;
    }

    @Override
    public PermissionSnapshot getPermissionSnapshot(String userId, String organizationId) {
        Set<String> permissionCodes = getEffectivePermissions(userId, organizationId);

        Membership membership = membershipRepo.findByOrgAndUser(organizationId, userId).orElse(null);
        Organization org = orgRepo.findById(organizationId).orElse(null);

        Set<String> roleIds = new HashSet<>();
        Set<String> roleNames = new HashSet<>();
        if (membership != null) {
            List<String> rids = membershipRoleRepo.findRoleIdsByMembershipId(membership.getId());
            roleIds.addAll(rids);
            roleRepo.findByIds(new ArrayList<>(rids)).forEach(r -> roleNames.add(r.getName()));
        }

        return new PermissionSnapshot(
                organizationId,
                membership != null ? membership.getId() : null,
                roleIds, roleNames, permissionCodes,
                org != null ? org.getAuthorizationVersion() : 0
        );
    }
}