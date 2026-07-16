package com.github.houbb.core.identity.application.domain;

/**
 * Distributed job for coordinated task execution (P7.4).
 */
public class DistributedJob {

    private String id;
    private String jobType;          // grant-expiry, token-cleanup, invitation-expiry, etc.
    private String jobKey;           // unique key for deduplication
    private String organizationId;
    private String status;           // PENDING, RUNNING, COMPLETED, FAILED, CANCELLED
    private int priority;
    private String payloadJson;
    private long progressCurrent;
    private long progressTotal;
    private int maxAttempts;
    private int attemptCount;
    private Long nextAttemptAt;
    private String lockedByNodeId;
    private long lockFencingToken;
    private Long lockedUntil;
    private long createdAt;
    private Long startedAt;
    private Long completedAt;
    private long updatedAt;
    private long version;

    public DistributedJob() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }

    public String getJobKey() { return jobKey; }
    public void setJobKey(String jobKey) { this.jobKey = jobKey; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }

    public long getProgressCurrent() { return progressCurrent; }
    public void setProgressCurrent(long progressCurrent) { this.progressCurrent = progressCurrent; }

    public long getProgressTotal() { return progressTotal; }
    public void setProgressTotal(long progressTotal) { this.progressTotal = progressTotal; }

    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }

    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }

    public Long getNextAttemptAt() { return nextAttemptAt; }
    public void setNextAttemptAt(Long nextAttemptAt) { this.nextAttemptAt = nextAttemptAt; }

    public String getLockedByNodeId() { return lockedByNodeId; }
    public void setLockedByNodeId(String lockedByNodeId) { this.lockedByNodeId = lockedByNodeId; }

    public long getLockFencingToken() { return lockFencingToken; }
    public void setLockFencingToken(long lockFencingToken) { this.lockFencingToken = lockFencingToken; }

    public Long getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(Long lockedUntil) { this.lockedUntil = lockedUntil; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public Long getStartedAt() { return startedAt; }
    public void setStartedAt(Long startedAt) { this.startedAt = startedAt; }

    public Long getCompletedAt() { return completedAt; }
    public void setCompletedAt(Long completedAt) { this.completedAt = completedAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
