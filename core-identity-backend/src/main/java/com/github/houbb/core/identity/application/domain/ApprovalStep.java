package com.github.houbb.core.identity.application.domain;

/**
 * ApprovalStep — 审批步骤，审批流程中的一个阶段。
 *
 * 每个步骤定义审批模式（SINGLE/ALL/ANY_N/SEQUENTIAL/PARALLEL）、
 * 所需审批人数和审批人来源。
 *
 * Table: identity_approval_step
 */
public class ApprovalStep {

    private String id;
    private String approvalInstanceId;
    private int stepOrder;
    private String approvalMode;
    private int requiredApprovals;
    private String approverType;
    private String approverReference;
    private String status;
    private long dueAt;
    private long createdAt;

    public ApprovalStep() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getApprovalInstanceId() { return approvalInstanceId; }
    public void setApprovalInstanceId(String approvalInstanceId) { this.approvalInstanceId = approvalInstanceId; }
    public int getStepOrder() { return stepOrder; }
    public void setStepOrder(int stepOrder) { this.stepOrder = stepOrder; }
    public String getApprovalMode() { return approvalMode; }
    public void setApprovalMode(String approvalMode) { this.approvalMode = approvalMode; }
    public int getRequiredApprovals() { return requiredApprovals; }
    public void setRequiredApprovals(int requiredApprovals) { this.requiredApprovals = requiredApprovals; }
    public String getApproverType() { return approverType; }
    public void setApproverType(String approverType) { this.approverType = approverType; }
    public String getApproverReference() { return approverReference; }
    public void setApproverReference(String approverReference) { this.approverReference = approverReference; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getDueAt() { return dueAt; }
    public void setDueAt(long dueAt) { this.dueAt = dueAt; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}