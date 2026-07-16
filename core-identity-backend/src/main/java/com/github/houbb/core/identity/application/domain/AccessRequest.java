package com.github.houbb.core.identity.application.domain;

/**
 * AccessRequest — 用户发起的访问权限申请。
 *
 * 包含申请人、目标主体、申请的套餐、业务理由、有效期等信息。
 *
 * Table: identity_access_request
 */
public class AccessRequest {

    private String id;
    private String requesterUserId;
    private String targetSubjectType;
    private String targetSubjectId;
    private String organizationId;
    private String accessPackageId;
    private String businessReason;
    private String ticketReference;
    private long requestedStartAt;
    private long requestedEndAt;
    private String status;
    private String riskLevel;
    private String sodResult;
    private long submittedAt;
    private long completedAt;
    private long createdAt;
    private long updatedAt;
    private long version;

    public AccessRequest() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRequesterUserId() { return requesterUserId; }
    public void setRequesterUserId(String requesterUserId) { this.requesterUserId = requesterUserId; }
    public String getTargetSubjectType() { return targetSubjectType; }
    public void setTargetSubjectType(String targetSubjectType) { this.targetSubjectType = targetSubjectType; }
    public String getTargetSubjectId() { return targetSubjectId; }
    public void setTargetSubjectId(String targetSubjectId) { this.targetSubjectId = targetSubjectId; }
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    public String getAccessPackageId() { return accessPackageId; }
    public void setAccessPackageId(String accessPackageId) { this.accessPackageId = accessPackageId; }
    public String getBusinessReason() { return businessReason; }
    public void setBusinessReason(String businessReason) { this.businessReason = businessReason; }
    public String getTicketReference() { return ticketReference; }
    public void setTicketReference(String ticketReference) { this.ticketReference = ticketReference; }
    public long getRequestedStartAt() { return requestedStartAt; }
    public void setRequestedStartAt(long requestedStartAt) { this.requestedStartAt = requestedStartAt; }
    public long getRequestedEndAt() { return requestedEndAt; }
    public void setRequestedEndAt(long requestedEndAt) { this.requestedEndAt = requestedEndAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getSodResult() { return sodResult; }
    public void setSodResult(String sodResult) { this.sodResult = sodResult; }
    public long getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(long submittedAt) { this.submittedAt = submittedAt; }
    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}