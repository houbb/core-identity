package com.github.houbb.core.identity.api.response;

/**
 * Health response for internal API.
 */
public class HealthResponse {

    private String status;
    private String service;
    private long timestamp;

    public HealthResponse() {
    }

    public HealthResponse(String status, String service, long timestamp) {
        this.status = status;
        this.service = service;
        this.timestamp = timestamp;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getService() { return service; }
    public void setService(String service) { this.service = service; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}