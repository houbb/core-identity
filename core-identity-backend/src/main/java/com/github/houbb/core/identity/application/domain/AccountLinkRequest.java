package com.github.houbb.core.identity.application.domain;

/**
 * Account Link Request domain object — account binding confirmation workflow.
 *
 * P5: Created when a new external identity matches an existing local user by email.
 * Requires explicit confirmation or admin review before binding.
 * Table: identity_account_link_request
 */
public class AccountLinkRequest {

    private String id;
    private String connectionId;
    private String externalSubject;
    private String candidateUserId;
    private String externalEmail;
    private String status;
    private String riskLevel;
    private String verificationMethod;
    private Long expiresAt;
    private Long confirmedAt;
    private Long rejectedAt;
    private long createdAt;
    private long updatedAt;
    private long version;

    public AccountLinkRequest() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getConnectionId() { return connectionId; }
    public void setConnectionId(String connectionId) { this.connectionId = connectionId; }
    public String getExternalSubject() { return externalSubject; }
    public void setExternalSubject(String externalSubject) { this.externalSubject = externalSubject; }
    public String getCandidateUserId() { return candidateUserId; }
    public void setCandidateUserId(String candidateUserId) { this.candidateUserId = candidateUserId; }
    public String getExternalEmail() { return externalEmail; }
    public void setExternalEmail(String externalEmail) { this.externalEmail = externalEmail; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getVerificationMethod() { return verificationMethod; }
    public void setVerificationMethod(String verificationMethod) { this.verificationMethod = verificationMethod; }
    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }
    public Long getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(Long confirmedAt) { this.confirmedAt = confirmedAt; }
    public Long getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(Long rejectedAt) { this.rejectedAt = rejectedAt; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
