package com.github.houbb.core.identity.application.domain;

/**
 * SodPolicy — 职责分离策略。
 *
 * Table: identity_sod_policy
 */
public class SodPolicy {

    private String id;
    private String organizationId;
    private String name;
    private String policyType;
    private String enforcementMode;
    private String status;
    private String ownerUserId;
    private long createdAt;
    private long updatedAt;
    private long version;

    public SodPolicy() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPolicyType() { return policyType; }
    public void setPolicyType(String policyType) { this.policyType = policyType; }
    public String getEnforcementMode() { return enforcementMode; }
    public void setEnforcementMode(String enforcementMode) { this.enforcementMode = enforcementMode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(String ownerUserId) { this.ownerUserId = ownerUserId; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
