package com.github.houbb.core.identity.application.command;

import jakarta.validation.constraints.NotBlank;

/**
 * Command to write an outbox event.
 */
public class OutboxCommand {

    @NotBlank
    private String eventType;
    private int eventVersion = 1;
    @NotBlank
    private String aggregateType;
    @NotBlank
    private String aggregateId;
    @NotBlank
    private String payloadJson;
    private String headersJson;

    public OutboxCommand() {
    }

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
}