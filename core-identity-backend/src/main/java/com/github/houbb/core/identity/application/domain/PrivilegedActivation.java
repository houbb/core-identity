package com.github.houbb.core.identity.application.domain;

/**
 * PrivilegedActivation — 特权访问激活记录。
 *
 * 主体暂时激活一项"可激活特权"（Eligible → Active），
 * 记录激活原因、时长、关联的 Session 等信息。
 *
 * Table: identity_privileged_activation
 */
public class PrivilegedActivation {

    private String id;
    private String grantId;
    private String userId;
    private String organizationId;
    private String roleId;
    private String reason;
    private String ticketReference;
    private String status;
    private String authenticationLevel;
    private String sessionId;
    private long activatedAt;
    private long expiresAt;
    private long endedAt;
    private long createdAt;
    private long updatedAt;
    private long version;

    public PrivilegedActivation() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getGrantId() { return grantId; }
    public void setGrantId(String grantId) { this.grantId = grantId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    public String getRoleId() { return roleId; }
    public void setRoleId(String roleId) { this.roleId = roleId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getTicketReference() { return ticketReference; }
    public void setTicketReference(String ticketReference) { this.ticketReference = ticketReference; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAuthenticationLevel() { return authenticationLevel; }
    public void setAuthenticationLevel(String authenticationLevel) { this.authenticationLevel = authenticationLevel; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public long getActivatedAt() { return activatedAt; }
    public void setActivatedAt(long activatedAt) { this.activatedAt = activatedAt; }
    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }
    public long getEndedAt() { return endedAt; }
    public void setEndedAt(long endedAt) { this.endedAt = endedAt; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}