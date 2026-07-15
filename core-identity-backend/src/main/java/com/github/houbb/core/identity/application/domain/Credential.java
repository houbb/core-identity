package com.github.houbb.core.identity.application.domain;

/**
 * Credential domain object — password credentials.
 *
 * P4 extended fields: hashPolicyVersion, lastRehashedAt, compromisedDetectedAt.
 */
public class Credential {

    private String id;
    private String userId;
    private String credentialType;
    private String secretHash;
    private String algorithm;
    private String status;
    private int mustChange;
    private Long passwordChangedAt;
    private int failedAttemptCount;
    // P4 fields
    private String hashPolicyVersion;
    private Long lastRehashedAt;
    private Long compromisedDetectedAt;
    private long createdAt;
    private long updatedAt;
    private long version;

    public Credential() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getCredentialType() { return credentialType; }
    public void setCredentialType(String credentialType) { this.credentialType = credentialType; }
    public String getSecretHash() { return secretHash; }
    public void setSecretHash(String secretHash) { this.secretHash = secretHash; }
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getMustChange() { return mustChange; }
    public void setMustChange(int mustChange) { this.mustChange = mustChange; }
    public Long getPasswordChangedAt() { return passwordChangedAt; }
    public void setPasswordChangedAt(Long passwordChangedAt) { this.passwordChangedAt = passwordChangedAt; }
    public int getFailedAttemptCount() { return failedAttemptCount; }
    public void setFailedAttemptCount(int failedAttemptCount) { this.failedAttemptCount = failedAttemptCount; }
    public String getHashPolicyVersion() { return hashPolicyVersion; }
    public void setHashPolicyVersion(String hashPolicyVersion) { this.hashPolicyVersion = hashPolicyVersion; }
    public Long getLastRehashedAt() { return lastRehashedAt; }
    public void setLastRehashedAt(Long lastRehashedAt) { this.lastRehashedAt = lastRehashedAt; }
    public Long getCompromisedDetectedAt() { return compromisedDetectedAt; }
    public void setCompromisedDetectedAt(Long compromisedDetectedAt) { this.compromisedDetectedAt = compromisedDetectedAt; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}