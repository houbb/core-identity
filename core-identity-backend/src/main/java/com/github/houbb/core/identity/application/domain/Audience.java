package com.github.houbb.core.identity.application.domain;

/**
 * Audience — a resource server that Access Tokens can be issued for.
 *
 * Table: identity_audience
 */
public class Audience {

    private String id;
    private String audienceCode;
    private String serviceName;
    private String description;
    private int issuerAllowed;
    private int tokenTtlSeconds;
    private String status;
    private long createdAt;
    private long updatedAt;
    private long version;

    public Audience() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAudienceCode() { return audienceCode; }
    public void setAudienceCode(String audienceCode) { this.audienceCode = audienceCode; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getIssuerAllowed() { return issuerAllowed; }
    public void setIssuerAllowed(int issuerAllowed) { this.issuerAllowed = issuerAllowed; }
    public int getTokenTtlSeconds() { return tokenTtlSeconds; }
    public void setTokenTtlSeconds(int tokenTtlSeconds) { this.tokenTtlSeconds = tokenTtlSeconds; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}