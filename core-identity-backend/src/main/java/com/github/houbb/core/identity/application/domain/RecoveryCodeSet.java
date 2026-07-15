package com.github.houbb.core.identity.application.domain;

/**
 * Recovery code set domain object — a batch of recovery codes for an account.
 */
public class RecoveryCodeSet {

    private String id;
    private String userId;
    private String status;
    private int totalCount;
    private int remainingCount;
    private long generatedAt;
    private Long revokedAt;
    private long version;

    public RecoveryCodeSet() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public int getRemainingCount() { return remainingCount; }
    public void setRemainingCount(int remainingCount) { this.remainingCount = remainingCount; }
    public long getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(long generatedAt) { this.generatedAt = generatedAt; }
    public Long getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Long revokedAt) { this.revokedAt = revokedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
