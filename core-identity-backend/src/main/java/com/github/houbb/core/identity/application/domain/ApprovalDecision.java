package com.github.houbb.core.identity.application.domain;

/**
 * ApprovalDecision — 审批决定，审批人对某个审批步骤做出的决定。
 *
 * 审批决定只追加，不覆盖历史决定。
 *
 * Table: identity_approval_decision
 */
public class ApprovalDecision {

    private String id;
    private String approvalStepId;
    private String approverUserId;
    private String decision;
    private String reason;
    private long decidedAt;
    private String authenticationLevel;
    private String requestId;

    public ApprovalDecision() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getApprovalStepId() { return approvalStepId; }
    public void setApprovalStepId(String approvalStepId) { this.approvalStepId = approvalStepId; }
    public String getApproverUserId() { return approverUserId; }
    public void setApproverUserId(String approverUserId) { this.approverUserId = approverUserId; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public long getDecidedAt() { return decidedAt; }
    public void setDecidedAt(long decidedAt) { this.decidedAt = decidedAt; }
    public String getAuthenticationLevel() { return authenticationLevel; }
    public void setAuthenticationLevel(String authenticationLevel) { this.authenticationLevel = authenticationLevel; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
}