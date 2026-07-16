package com.github.houbb.core.identity.application.domain;

/**
 * Domain Verification domain object — DNS TXT challenge tracking.
 *
 * P5: Records verification challenge attempts and results.
 * Table: identity_domain_verification
 */
public class DomainVerification {

    private String id;
    private String domainId;
    private String challengeHash;
    private String expectedRecordName;
    private String method;
    private String status;
    private int attemptCount;
    private Long expiresAt;
    private Long verifiedAt;
    private long createdAt;
    private long updatedAt;
    private long version;

    public DomainVerification() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDomainId() { return domainId; }
    public void setDomainId(String domainId) { this.domainId = domainId; }
    public String getChallengeHash() { return challengeHash; }
    public void setChallengeHash(String challengeHash) { this.challengeHash = challengeHash; }
    public String getExpectedRecordName() { return expectedRecordName; }
    public void setExpectedRecordName(String expectedRecordName) { this.expectedRecordName = expectedRecordName; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }
    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }
    public Long getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Long verifiedAt) { this.verifiedAt = verifiedAt; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
