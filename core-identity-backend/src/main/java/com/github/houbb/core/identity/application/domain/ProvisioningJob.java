package com.github.houbb.core.identity.application.domain;

/**
 * Provisioning Job domain object — tracks SCIM sync batch operations.
 *
 * P5: Each provisioning job records total/success/failed item counts
 * for monitoring and retry.
 * Table: identity_provisioning_job
 */
public class ProvisioningJob {

    private String id;
    private String organizationId;
    private String connectionId;
    private String jobType;
    private String status;
    private int totalItems;
    private int successItems;
    private int failedItems;
    private Long startedAt;
    private Long completedAt;
    private long createdAt;
    private long updatedAt;
    private long version;

    public ProvisioningJob() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    public String getConnectionId() { return connectionId; }
    public void setConnectionId(String connectionId) { this.connectionId = connectionId; }
    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getTotalItems() { return totalItems; }
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }
    public int getSuccessItems() { return successItems; }
    public void setSuccessItems(int successItems) { this.successItems = successItems; }
    public int getFailedItems() { return failedItems; }
    public void setFailedItems(int failedItems) { this.failedItems = failedItems; }
    public Long getStartedAt() { return startedAt; }
    public void setStartedAt(Long startedAt) { this.startedAt = startedAt; }
    public Long getCompletedAt() { return completedAt; }
    public void setCompletedAt(Long completedAt) { this.completedAt = completedAt; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
