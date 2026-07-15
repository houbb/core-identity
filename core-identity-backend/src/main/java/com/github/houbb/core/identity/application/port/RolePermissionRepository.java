package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.RolePermission;

import java.util.List;

/**
 * Repository for identity_role_permission.
 */
public interface RolePermissionRepository {

    void save(RolePermission rp);

    void deleteByRoleAndPermission(String roleId, String permissionId);

    void deleteAllByRoleId(String roleId);

    List<RolePermission> findByRoleId(String roleId);

    List<RolePermission> findByPermissionId(String permissionId);

    List<String> findPermissionIdsByRoleId(String roleId);

    List<String> findPermissionCodesByRoleIds(List<String> roleIds);

    /**
     * Transactionally replace all permissions for a role.
     */
    void replacePermissions(String roleId, List<String> permissionIds, String grantedBy, long now);
}