package com.github.houbb.core.identity.application.domain;

/**
 * Mapping between a Scope and its effective Permissions.
 *
 * Table: identity_scope_permission
 */
public class ScopePermission {

    private String id;
    private String scopeId;
    private String permissionId;
    private long createdAt;

    public ScopePermission() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getScopeId() { return scopeId; }
    public void setScopeId(String scopeId) { this.scopeId = scopeId; }
    public String getPermissionId() { return permissionId; }
    public void setPermissionId(String permissionId) { this.permissionId = permissionId; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}