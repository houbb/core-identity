package com.github.houbb.core.identity.application.domain;

/**
 * Account recovery domain object — tracks the recovery process.
 */
public class AccountRecovery {

    private String id;
    private String userId;
    private String recoveryType;
    private String status;
    private String riskLevel;
    private String requiredEvidenceLevel;
    private String initiatedIp;
    private String initiatedDeviceId;
    private Long coolingOffUntil;
    private String approvedBy;
    private String rejectedBy;
    private Long completedAt;
    private Long cancelledAt;
    private long createdAt;
    private long updatedAt;
    private long version;

    public AccountRecovery() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getRecoveryType() { return recoveryType; }
    public void setRecoveryType(String recoveryType) { this.recoveryType = recoveryType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getRequiredEvidenceLevel() { return requiredEvidenceLevel; }
    public void setRequiredEvidenceLevel(String requiredEvidenceLevel) { this.requiredEvidenceLevel = requiredEvidenceLevel; }
    public String getInitiatedIp() { return initiatedIp; }
    public void setInitiatedIp(String initiatedIp) { this.initiatedIp = initiatedIp; }
    public String getInitiatedDeviceId() { return initiatedDeviceId; }
    public void setInitiatedDeviceId(String initiatedDeviceId) { this.initiatedDeviceId = initiatedDeviceId; }
    public Long getCoolingOffUntil() { return coolingOffUntil; }
    public void setCoolingOffUntil(Long coolingOffUntil) { this.coolingOffUntil = coolingOffUntil; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public String getRejectedBy() { return rejectedBy; }
    public void setRejectedBy(String rejectedBy) { this.rejectedBy = rejectedBy; }
    public Long getCompletedAt() { return completedAt; }
    public void setCompletedAt(Long completedAt) { this.completedAt = completedAt; }
    public Long getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(Long cancelledAt) { this.cancelledAt = cancelledAt; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
