package com.github.houbb.core.identity.application.domain;

/**
 * AccessPackage — 面向用户申请的一组访问权益。
 *
 * 用户不应该面对数百个 Permission 复选框，
 * Access Package 将相关权益打包成可理解的套餐。
 *
 * Table: identity_access_package
 */
public class AccessPackage {

    private String id;
    private String organizationId;
    private String packageCode;
    private String name;
    private String description;
    private String packageType;
    private String riskLevel;
    private int requestable;
    private long defaultDurationSeconds;
    private long maxDurationSeconds;
    private String requiredAuthLevel;
    private String ownerUserId;
    private String approvalPolicyJson;
    private String eligibilityPolicyJson;
    private String status;
    private long createdAt;
    private long updatedAt;
    private long version;

    public AccessPackage() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    public String getPackageCode() { return packageCode; }
    public void setPackageCode(String packageCode) { this.packageCode = packageCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPackageType() { return packageType; }
    public void setPackageType(String packageType) { this.packageType = packageType; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public int getRequestable() { return requestable; }
    public void setRequestable(int requestable) { this.requestable = requestable; }
    public long getDefaultDurationSeconds() { return defaultDurationSeconds; }
    public void setDefaultDurationSeconds(long defaultDurationSeconds) { this.defaultDurationSeconds = defaultDurationSeconds; }
    public long getMaxDurationSeconds() { return maxDurationSeconds; }
    public void setMaxDurationSeconds(long maxDurationSeconds) { this.maxDurationSeconds = maxDurationSeconds; }
    public String getRequiredAuthLevel() { return requiredAuthLevel; }
    public void setRequiredAuthLevel(String requiredAuthLevel) { this.requiredAuthLevel = requiredAuthLevel; }
    public String getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(String ownerUserId) { this.ownerUserId = ownerUserId; }
    public String getApprovalPolicyJson() { return approvalPolicyJson; }
    public void setApprovalPolicyJson(String approvalPolicyJson) { this.approvalPolicyJson = approvalPolicyJson; }
    public String getEligibilityPolicyJson() { return eligibilityPolicyJson; }
    public void setEligibilityPolicyJson(String eligibilityPolicyJson) { this.eligibilityPolicyJson = eligibilityPolicyJson; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
