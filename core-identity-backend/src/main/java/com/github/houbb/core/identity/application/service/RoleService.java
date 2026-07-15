package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.Role;

import java.util.List;

/**
 * Role management service — CRUD roles and assign permissions.
 */
public interface RoleService {

    /**
     * Initialize built-in roles for a newly created TEAM organization.
     */
    List<Role> initializeBuiltInRoles(String organizationId, long now);

    /**
     * Create a custom role.
     */
    Role createCustomRole(String organizationId, String name, String description, String creatorId);

    /**
     * Update a role's name and description (cannot change protected fields).
     */
    Role updateRole(String organizationId, String roleId, String name, String description);

    /**
     * Delete a custom role. Must not be in use by members.
     */
    void deleteRole(String organizationId, String roleId);

    /**
     * Assign permissions to a role (transactionally replaces existing).
     */
    void assignPermissions(String organizationId, String roleId, List<String> permissionIds, String operatorId);

    /**
     * Get all roles for an organization.
     */
    List<Role> getRoles(String organizationId);

    /**
     * Get a specific role.
     */
    Role getRole(String organizationId, String roleId);

    /**
     * Get the built-in role keys that should be created for every TEAM organization.
     */
    List<BuiltInRoleDef> getBuiltInRoleDefs();

    /**
     * Definition of a built-in role.
     */
    record BuiltInRoleDef(String roleKey, String name, String description, int sortOrder,
                          List<String> corePermissionCodes) {
    }
}