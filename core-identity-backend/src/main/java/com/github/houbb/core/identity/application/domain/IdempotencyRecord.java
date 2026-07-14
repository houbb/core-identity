package com.github.houbb.core.identity.application.domain;

/**
 * Idempotency record domain object.
 */
public class IdempotencyRecord {

    private String id;
    private String idempotencyKey;
    private String scope;
    private String requestHash;
    private String status;
    private Integer responseStatus;
    private String responseBody;
    private Long lockedUntil;
    private long expiresAt;
    private long createdAt;
    private long updatedAt;

    public IdempotencyRecord() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public String getRequestHash() { return requestHash; }
    public void setRequestHash(String requestHash) { this.requestHash = requestHash; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getResponseStatus() { return responseStatus; }
    public void setResponseStatus(Integer responseStatus) { this.responseStatus = responseStatus; }

    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }

    public Long getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(Long lockedUntil) { this.lockedUntil = lockedUntil; }

    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}