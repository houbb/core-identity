package com.github.houbb.core.identity.application.domain;

/**
 * RolePermission — many-to-many link between Role and Permission.
 *
 * Table: identity_role_permission
 */
public class RolePermission {

    private String roleId;
    private String permissionId;
    private String grantedBy;
    private long createdAt;

    public RolePermission() {
    }

    public String getRoleId() { return roleId; }
    public void setRoleId(String roleId) { this.roleId = roleId; }
    public String getPermissionId() { return permissionId; }
    public void setPermissionId(String permissionId) { this.permissionId = permissionId; }
    public String getGrantedBy() { return grantedBy; }
    public void setGrantedBy(String grantedBy) { this.grantedBy = grantedBy; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}