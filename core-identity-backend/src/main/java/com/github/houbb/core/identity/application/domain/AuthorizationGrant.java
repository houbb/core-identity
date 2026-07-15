package com.github.houbb.core.identity.application.domain;

public class AuthorizationGrant {
    private String id; private String clientId; private String userId; private String organizationId;
    private String status; private long firstGrantedAt; private Long lastUsedAt; private Long revokedAt;
    private long createdAt; private long updatedAt; private long version;
    public AuthorizationGrant() {}
    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getClientId() { return clientId; } public void setClientId(String clientId) { this.clientId = clientId; }
    public String getUserId() { return userId; } public void setUserId(String userId) { this.userId = userId; }
    public String getOrganizationId() { return organizationId; } public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public long getFirstGrantedAt() { return firstGrantedAt; } public void setFirstGrantedAt(long firstGrantedAt) { this.firstGrantedAt = firstGrantedAt; }
    public Long getLastUsedAt() { return lastUsedAt; } public void setLastUsedAt(Long lastUsedAt) { this.lastUsedAt = lastUsedAt; }
    public Long getRevokedAt() { return revokedAt; } public void setRevokedAt(Long revokedAt) { this.revokedAt = revokedAt; }
    public long getCreatedAt() { return createdAt; } public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; } public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; } public void setVersion(long version) { this.version = version; }
}