package com.github.houbb.core.identity.application.service;

/**
 * SCIM 2.0 Service — Core Identity as a SCIM Service Provider.
 *
 * P5: Full SCIM 2.0 Users and Groups CRUD. SCIM deactivation suspends Membership
 * and revokes sessions, but never deletes the global User.
 */
public interface ScimService {

    // === SCIM Users ===
    String createScimUser(String connectionId, String externalId, String userName,
                          String displayName, String email, boolean active, long now);

    void updateScimUser(String connectionId, String scimUserId, String displayName, String email,
                        boolean active, long now);

    void deactivateScimUser(String connectionId, String scimUserId, long now);

    // === SCIM Groups ===
    String createScimGroup(String connectionId, String externalId, String displayName, long now);

    void updateScimGroup(String connectionId, String scimGroupId, String displayName, long now);

    void deleteScimGroup(String connectionId, String scimGroupId, long now);

    void addScimGroupMember(String connectionId, String scimGroupId, String externalIdentityId, long now);

    void removeScimGroupMember(String connectionId, String scimGroupId, String externalIdentityId, long now);

    // === SCIM Group to Role Mapping ===
    void createGroupRoleMapping(String connectionId, String scimGroupId, String roleId,
                                String mappingMode, String createdBy, long now);

    void applyGroupRoleMappings(String connectionId, String scimGroupId, long now);
}
