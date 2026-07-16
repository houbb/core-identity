package com.github.houbb.core.identity.application.domain;

/**
 * Entitlement — 可被治理和复核的一项访问权益。
 *
 * 可以表示某个 Role、某个 Permission、某个 OAuth Scope、
 * 某个 Service Account Credential 或某个管理控制台身份。
 *
 * Table: identity_entitlement
 */
public class Entitlement {

    private String id;
    private String organizationId;
    private String entitlementType;
    private String targetId;
    private String code;
    private String name;
    private String riskLevel;
    private String ownerUserId;
    private String status;
    private int reviewFrequencyDays;
    private long createdAt;
    private long updatedAt;
    private long version;

    public Entitlement() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    public String getEntitlementType() { return entitlementType; }
    public void setEntitlementType(String entitlementType) { this.entitlementType = entitlementType; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(String ownerUserId) { this.ownerUserId = ownerUserId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getReviewFrequencyDays() { return reviewFrequencyDays; }
    public void setReviewFrequencyDays(int reviewFrequencyDays) { this.reviewFrequencyDays = reviewFrequencyDays; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
