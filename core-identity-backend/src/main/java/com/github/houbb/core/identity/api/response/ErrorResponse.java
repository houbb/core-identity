package com.github.houbb.core.identity.api.response;

import java.time.Instant;

/**
 * RFC 7807 Problem Detail error response.
 */
public class ErrorResponse {

    private String type;
    private String title;
    private int status;
    private String detail;
    private String errorCode;
    private String requestId;
    private String timestamp;

    public ErrorResponse() {
    }

    public static ErrorResponse of(int status, String title, String detail, String errorCode, String requestId) {
        ErrorResponse r = new ErrorResponse();
        r.type = "https://core.example/problems/" + errorCode.toLowerCase().replace("_", "-");
        r.title = title;
        r.status = status;
        r.detail = detail;
        r.errorCode = errorCode;
        r.requestId = requestId;
        r.timestamp = Instant.now().toString();
        return r;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}