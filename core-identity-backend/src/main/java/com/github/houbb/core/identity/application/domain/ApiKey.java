package com.github.houbb.core.identity.application.domain;

public class ApiKey {
    private String id; private String keyPrefix; private String keyHash; private String name;
    private String ownerType; private String ownerId; private String organizationId; private String status;
    private Long expiresAt; private Long lastUsedAt; private String lastUsedIp;
    private long createdAt; private Long revokedAt; private long version;
    public ApiKey() {}
    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getKeyPrefix() { return keyPrefix; } public void setKeyPrefix(String keyPrefix) { this.keyPrefix = keyPrefix; }
    public String getKeyHash() { return keyHash; } public void setKeyHash(String keyHash) { this.keyHash = keyHash; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getOwnerType() { return ownerType; } public void setOwnerType(String ownerType) { this.ownerType = ownerType; }
    public String getOwnerId() { return ownerId; } public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getOrganizationId() { return organizationId; } public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public Long getExpiresAt() { return expiresAt; } public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }
    public Long getLastUsedAt() { return lastUsedAt; } public void setLastUsedAt(Long lastUsedAt) { this.lastUsedAt = lastUsedAt; }
    public String getLastUsedIp() { return lastUsedIp; } public void setLastUsedIp(String lastUsedIp) { this.lastUsedIp = lastUsedIp; }
    public long getCreatedAt() { return createdAt; } public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public Long getRevokedAt() { return revokedAt; } public void setRevokedAt(Long revokedAt) { this.revokedAt = revokedAt; }
    public long getVersion() { return version; } public void setVersion(long version) { this.version = version; }
}