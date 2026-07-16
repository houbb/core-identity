package com.github.houbb.core.identity.application.domain;

/**
 * AccessGrant — 已经授予主体的长期或临时访问权。
 *
 * 记录：授予什么、授予给谁、为何授予、谁批准、何时生效、何时到期、来源是什么。
 *
 * Table: identity_access_grant
 */
public class AccessGrant {

    private String id;
    private String subjectType;
    private String subjectId;
    private String organizationId;
    private String entitlementId;
    private String sourceType;
    private String sourceId;
    private String grantType;
    private String status;
    private long validFrom;
    private long expiresAt;
    private String grantedBy;
    private String revokedBy;
    private long revokedAt;
    private String revokeReason;
    private long lastUsedAt;
    private long createdAt;
    private long updatedAt;
    private long version;

    public AccessGrant() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSubjectType() { return subjectType; }
    public void setSubjectType(String subjectType) { this.subjectType = subjectType; }
    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    public String getEntitlementId() { return entitlementId; }
    public void setEntitlementId(String entitlementId) { this.entitlementId = entitlementId; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
    public String getGrantType() { return grantType; }
    public void setGrantType(String grantType) { this.grantType = grantType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getValidFrom() { return validFrom; }
    public void setValidFrom(long validFrom) { this.validFrom = validFrom; }
    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }
    public String getGrantedBy() { return grantedBy; }
    public void setGrantedBy(String grantedBy) { this.grantedBy = grantedBy; }
    public String getRevokedBy() { return revokedBy; }
    public void setRevokedBy(String revokedBy) { this.revokedBy = revokedBy; }
    public long getRevokedAt() { return revokedAt; }
    public void setRevokedAt(long revokedAt) { this.revokedAt = revokedAt; }
    public String getRevokeReason() { return revokeReason; }
    public void setRevokeReason(String revokeReason) { this.revokeReason = revokeReason; }
    public long getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(long lastUsedAt) { this.lastUsedAt = lastUsedAt; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}