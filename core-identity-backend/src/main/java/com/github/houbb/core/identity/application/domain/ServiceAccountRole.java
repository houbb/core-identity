package com.github.houbb.core.identity.application.domain;

/**
 * Role assignment for Service Accounts.
 * Service Accounts get permissions through roles within their organization.
 *
 * Table: identity_service_account_role
 */
public class ServiceAccountRole {

    private String id;
    private String serviceAccountId;
    private String roleId;
    private String assignedBy;
    private long createdAt;

    public ServiceAccountRole() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getServiceAccountId() { return serviceAccountId; }
    public void setServiceAccountId(String serviceAccountId) { this.serviceAccountId = serviceAccountId; }
    public String getRoleId() { return roleId; }
    public void setRoleId(String roleId) { this.roleId = roleId; }
    public String getAssignedBy() { return assignedBy; }
    public void setAssignedBy(String assignedBy) { this.assignedBy = assignedBy; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}