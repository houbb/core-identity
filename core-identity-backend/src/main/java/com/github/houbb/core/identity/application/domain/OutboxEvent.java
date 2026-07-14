package com.github.houbb.core.identity.application.domain;

/**
 * Outbox event domain object.
 */
public class OutboxEvent {

    private String id;
    private String eventType;
    private int eventVersion;
    private String aggregateType;
    private String aggregateId;
    private String payloadJson;
    private String headersJson;
    private String status;
    private int attemptCount;
    private Long nextAttemptAt;
    private Long publishedAt;
    private String lastError;
    private long createdAt;
    private long updatedAt;
    private long version;

    public OutboxEvent() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public int getEventVersion() { return eventVersion; }
    public void setEventVersion(int eventVersion) { this.eventVersion = eventVersion; }

    public String getAggregateType() { return aggregateType; }
    public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }

    public String getAggregateId() { return aggregateId; }
    public void setAggregateId(String aggregateId) { this.aggregateId = aggregateId; }

    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }

    public String getHeadersJson() { return headersJson; }
    public void setHeadersJson(String headersJson) { this.headersJson = headersJson; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }

    public Long getNextAttemptAt() { return nextAttemptAt; }
    public void setNextAttemptAt(Long nextAttemptAt) { this.nextAttemptAt = nextAttemptAt; }

    public Long getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Long publishedAt) { this.publishedAt = publishedAt; }

    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}