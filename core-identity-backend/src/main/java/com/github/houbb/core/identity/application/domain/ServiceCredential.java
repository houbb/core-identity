package com.github.houbb.core.identity.application.domain;

/**
 * Service Account credential — client_id + client_secret for OAuth Client Credentials flow.
 * Separate from API Keys; used for machine-to-machine authentication.
 *
 * Table: identity_service_credential
 */
public class ServiceCredential {

    private String id;
    private String serviceAccountId;
    private String clientId;
    private String secretPrefix;
    private String secretHash;
    private String name;
    private String status;
    private Long expiresAt;
    private Long lastUsedAt;
    private long createdAt;
    private Long revokedAt;

    public ServiceCredential() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getServiceAccountId() { return serviceAccountId; }
    public void setServiceAccountId(String serviceAccountId) { this.serviceAccountId = serviceAccountId; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getSecretPrefix() { return secretPrefix; }
    public void setSecretPrefix(String secretPrefix) { this.secretPrefix = secretPrefix; }
    public String getSecretHash() { return secretHash; }
    public void setSecretHash(String secretHash) { this.secretHash = secretHash; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }
    public Long getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(Long lastUsedAt) { this.lastUsedAt = lastUsedAt; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public Long getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Long revokedAt) { this.revokedAt = revokedAt; }
}