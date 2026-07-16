package com.github.houbb.core.identity.application.domain;

/**
 * Verified Domain domain object — enterprise domain ownership proof.
 *
 * P5: Domain verification is a prerequisite for SSO enforcement.
 * Table: identity_verified_domain
 */
public class VerifiedDomain {

    private String id;
    private String organizationId;
    private String domainName;
    private String status;
    private String verificationMethod;
    private Long verifiedAt;
    private Long lastCheckedAt;
    private Long expiresAt;
    private String conflictReason;
    private String createdBy;
    private long createdAt;
    private long updatedAt;
    private long version;

    public VerifiedDomain() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    public String getDomainName() { return domainName; }
    public void setDomainName(String domainName) { this.domainName = domainName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getVerificationMethod() { return verificationMethod; }
    public void setVerificationMethod(String verificationMethod) { this.verificationMethod = verificationMethod; }
    public Long getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Long verifiedAt) { this.verifiedAt = verifiedAt; }
    public Long getLastCheckedAt() { return lastCheckedAt; }
    public void setLastCheckedAt(Long lastCheckedAt) { this.lastCheckedAt = lastCheckedAt; }
    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }
    public String getConflictReason() { return conflictReason; }
    public void setConflictReason(String conflictReason) { this.conflictReason = conflictReason; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
