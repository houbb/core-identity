package com.github.houbb.core.identity.application.domain;

/**
 * Step-up grant domain object — temporary elevated auth for sensitive operations.
 */
public class StepUpGrant {

    private String id;
    private String userId;
    private String sessionId;
    private String authenticationLevel;
    private String allowedActionsJson;
    private String status;
    private long issuedAt;
    private long expiresAt;
    private Long consumedAt;
    private long createdAt;
    private long version;

    public StepUpGrant() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getAuthenticationLevel() { return authenticationLevel; }
    public void setAuthenticationLevel(String authenticationLevel) { this.authenticationLevel = authenticationLevel; }
    public String getAllowedActionsJson() { return allowedActionsJson; }
    public void setAllowedActionsJson(String allowedActionsJson) { this.allowedActionsJson = allowedActionsJson; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getIssuedAt() { return issuedAt; }
    public void setIssuedAt(long issuedAt) { this.issuedAt = issuedAt; }
    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }
    public Long getConsumedAt() { return consumedAt; }
    public void setConsumedAt(Long consumedAt) { this.consumedAt = consumedAt; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
