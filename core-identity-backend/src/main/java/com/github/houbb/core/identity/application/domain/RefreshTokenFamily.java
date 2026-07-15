package com.github.houbb.core.identity.application.domain;

public class RefreshTokenFamily {
    private String id; private String grantId; private String clientId; private String userId;
    private String sessionId; private String status; private String revokedReason;
    private long createdAt; private Long revokedAt;
    public RefreshTokenFamily() {}
    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getGrantId() { return grantId; } public void setGrantId(String grantId) { this.grantId = grantId; }
    public String getClientId() { return clientId; } public void setClientId(String clientId) { this.clientId = clientId; }
    public String getUserId() { return userId; } public void setUserId(String userId) { this.userId = userId; }
    public String getSessionId() { return sessionId; } public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public String getRevokedReason() { return revokedReason; } public void setRevokedReason(String revokedReason) { this.revokedReason = revokedReason; }
    public long getCreatedAt() { return createdAt; } public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public Long getRevokedAt() { return revokedAt; } public void setRevokedAt(Long revokedAt) { this.revokedAt = revokedAt; }
}