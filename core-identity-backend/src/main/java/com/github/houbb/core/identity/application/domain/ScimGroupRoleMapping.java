package com.github.houbb.core.identity.application.domain;

/**
 * SCIM Group-Role Mapping domain object — maps external directory groups to Core Identity roles.
 *
 * P5: Defines the bridge between enterprise groups and platform permissions.
 * Mapping modes: ADD_ONLY (grants role but doesn't remove local roles) or AUTHORITATIVE.
 * Protected roles (OWNER, SUPER_ADMIN, BREAK_GLASS) cannot be granted via group mapping.
 * Table: identity_scim_group_role_mapping
 */
public class ScimGroupRoleMapping {

    private String id;
    private String groupId;
    private String roleId;
    private String mappingMode;
    private String status;
    private String createdBy;
    private long createdAt;
    private long updatedAt;
    private long version;

    public ScimGroupRoleMapping() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public String getRoleId() { return roleId; }
    public void setRoleId(String roleId) { this.roleId = roleId; }
    public String getMappingMode() { return mappingMode; }
    public void setMappingMode(String mappingMode) { this.mappingMode = mappingMode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
