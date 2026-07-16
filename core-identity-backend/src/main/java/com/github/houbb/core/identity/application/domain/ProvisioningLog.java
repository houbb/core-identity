package com.github.houbb.core.identity.application.domain;

/**
 * Provisioning Log domain object — individual SCIM operation result records.
 *
 * P5: Immutable append-only log for each SCIM create/update/delete operation.
 * No version or updatedAt — events are immutable.
 * Table: identity_provisioning_log
 */
public class ProvisioningLog {

    private String id;
    private String jobId;
    private String resourceType;
    private String externalId;
    private String operation;
    private String result;
    private String errorCode;
    private String errorMessage;
    private String requestId;
    private long occurredAt;
    private long createdAt;

    public ProvisioningLog() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public long getOccurredAt() { return occurredAt; }
    public void setOccurredAt(long occurredAt) { this.occurredAt = occurredAt; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
