package com.github.houbb.core.identity.application.domain;

/**
 * ApprovalInstance — 审批实例，表示一次审批流程的完整记录。
 *
 * Table: identity_approval_instance
 */
public class ApprovalInstance {

    private String id;
    private String requestType;
    private String requestId;
    private String status;
    private int currentStep;
    private long createdAt;
    private long completedAt;
    private long version;

    public ApprovalInstance() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getCurrentStep() { return currentStep; }
    public void setCurrentStep(int currentStep) { this.currentStep = currentStep; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}