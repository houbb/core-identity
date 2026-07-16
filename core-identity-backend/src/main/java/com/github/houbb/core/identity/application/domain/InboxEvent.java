package com.github.houbb.core.identity.application.domain;

/**
 * Inbox event record for idempotent event consumption (P7.3).
 * <p>
 * Each consumer records processed events to prevent duplicate processing
 * in at-least-once delivery scenarios.
 */
public class InboxEvent {

    private String id;
    private String consumerName;
    private String eventId;
    private String eventType;
    private String status;          // PROCESSED, DUPLICATE
    private String payloadHash;
    private long receivedAt;
    private Long processedAt;
    private String errorCode;
    private int retryCount;
    private long version;

    public InboxEvent() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getConsumerName() { return consumerName; }
    public void setConsumerName(String consumerName) { this.consumerName = consumerName; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPayloadHash() { return payloadHash; }
    public void setPayloadHash(String payloadHash) { this.payloadHash = payloadHash; }

    public long getReceivedAt() { return receivedAt; }
    public void setReceivedAt(long receivedAt) { this.receivedAt = receivedAt; }

    public Long getProcessedAt() { return processedAt; }
    public void setProcessedAt(Long processedAt) { this.processedAt = processedAt; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
