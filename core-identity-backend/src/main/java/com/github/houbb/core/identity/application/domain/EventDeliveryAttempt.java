package com.github.houbb.core.identity.application.domain;

/**
 * Records a single delivery attempt for an outbox event (P7.3).
 */
public class EventDeliveryAttempt {

    private String id;
    private String outboxEventId;
    private String destination;
    private int attemptNumber;
    private String status;           // PENDING, SUCCESS, FAILED, DEAD_LETTER
    private Integer responseCode;
    private String errorCode;
    private long startedAt;
    private Long completedAt;
    private Long nextAttemptAt;

    public EventDeliveryAttempt() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOutboxEventId() { return outboxEventId; }
    public void setOutboxEventId(String outboxEventId) { this.outboxEventId = outboxEventId; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public int getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(int attemptNumber) { this.attemptNumber = attemptNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getResponseCode() { return responseCode; }
    public void setResponseCode(Integer responseCode) { this.responseCode = responseCode; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public long getStartedAt() { return startedAt; }
    public void setStartedAt(long startedAt) { this.startedAt = startedAt; }

    public Long getCompletedAt() { return completedAt; }
    public void setCompletedAt(Long completedAt) { this.completedAt = completedAt; }

    public Long getNextAttemptAt() { return nextAttemptAt; }
    public void setNextAttemptAt(Long nextAttemptAt) { this.nextAttemptAt = nextAttemptAt; }
}
