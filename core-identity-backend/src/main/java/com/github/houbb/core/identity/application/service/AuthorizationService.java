package com.github.houbb.core.identity.application.service;

import java.util.Set;

/**
 * Authorization service — check and require permissions within an organization context.
 */
public interface AuthorizationService {

    AuthorizationResult check(String userId, String organizationId, String permissionCode);

    void require(String userId, String organizationId, String permissionCode);

    Set<String> getEffectivePermissions(String userId, String organizationId);

    PermissionSnapshot getPermissionSnapshot(String userId, String organizationId);

    enum AuthorizationResult {
        ALLOW,
        DENY_USER_INACTIVE,
        DENY_ORGANIZATION_INACTIVE,
        DENY_MEMBERSHIP_NOT_FOUND,
        DENY_MEMBERSHIP_INACTIVE,
        DENY_PERMISSION_MISSING
    }

    record PermissionSnapshot(
            String organizationId,
            String membershipId,
            Set<String> roleIds,
            Set<String> roleNames,
            Set<String> permissionCodes,
            long permissionVersion
    ) {}
}