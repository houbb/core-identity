package com.github.houbb.core.identity.application.domain;

/**
 * SCIM Client domain object — SCIM 2.0 API client authentication tokens.
 *
 * P5: Each SCIM connection has independent Bearer tokens with scoped access.
 * Full token shown only once; stored as hash.
 * Table: identity_scim_client
 */
public class ScimClient {

    private String id;
    private String organizationId;
    private String connectionId;
    private String name;
    private String tokenPrefix;
    private String tokenHash;
    private String scopesJson;
    private String status;
    private Long expiresAt;
    private String ipAllowlistJson;
    private Long lastUsedAt;
    private String lastUsedIp;
    private long createdAt;
    private long updatedAt;
    private long version;

    public ScimClient() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    public String getConnectionId() { return connectionId; }
    public void setConnectionId(String connectionId) { this.connectionId = connectionId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTokenPrefix() { return tokenPrefix; }
    public void setTokenPrefix(String tokenPrefix) { this.tokenPrefix = tokenPrefix; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public String getScopesJson() { return scopesJson; }
    public void setScopesJson(String scopesJson) { this.scopesJson = scopesJson; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }
    public String getIpAllowlistJson() { return ipAllowlistJson; }
    public void setIpAllowlistJson(String ipAllowlistJson) { this.ipAllowlistJson = ipAllowlistJson; }
    public Long getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(Long lastUsedAt) { this.lastUsedAt = lastUsedAt; }
    public String getLastUsedIp() { return lastUsedIp; }
    public void setLastUsedIp(String lastUsedIp) { this.lastUsedIp = lastUsedIp; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
