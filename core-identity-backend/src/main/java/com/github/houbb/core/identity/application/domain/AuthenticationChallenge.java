package com.github.houbb.core.identity.application.domain;

/**
 * Authentication challenge domain object — TOTP, WebAuthn, step-up challenges.
 */
public class AuthenticationChallenge {

    private String id;
    private String userId;
    private String sessionId;
    private String challengeType;
    private String requiredLevel;
    private String allowedMethodsJson;
    private String challengeHash;
    private String contextJson;
    private String status;
    private int attemptCount;
    private long expiresAt;
    private Long completedAt;
    private long createdAt;
    private long version;

    public AuthenticationChallenge() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getChallengeType() { return challengeType; }
    public void setChallengeType(String challengeType) { this.challengeType = challengeType; }
    public String getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(String requiredLevel) { this.requiredLevel = requiredLevel; }
    public String getAllowedMethodsJson() { return allowedMethodsJson; }
    public void setAllowedMethodsJson(String allowedMethodsJson) { this.allowedMethodsJson = allowedMethodsJson; }
    public String getChallengeHash() { return challengeHash; }
    public void setChallengeHash(String challengeHash) { this.challengeHash = challengeHash; }
    public String getContextJson() { return contextJson; }
    public void setContextJson(String contextJson) { this.contextJson = contextJson; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }
    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }
    public Long getCompletedAt() { return completedAt; }
    public void setCompletedAt(Long completedAt) { this.completedAt = completedAt; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
