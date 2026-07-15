package com.github.houbb.core.identity.application.domain;

/**
 * Signing key used for JWT token signing and JWKS.
 *
 * Table: identity_signing_key
 */
public class SigningKey {

    private String id;
    private String keyId;
    private String algorithm;
    private String publicKey;
    private String encryptedPrivateKey;
    private String status;
    private Long activeFrom;
    private Long retireAfter;
    private long createdAt;
    private long updatedAt;

    public SigningKey() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getKeyId() { return keyId; }
    public void setKeyId(String keyId) { this.keyId = keyId; }
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
    public String getEncryptedPrivateKey() { return encryptedPrivateKey; }
    public void setEncryptedPrivateKey(String encryptedPrivateKey) { this.encryptedPrivateKey = encryptedPrivateKey; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getActiveFrom() { return activeFrom; }
    public void setActiveFrom(Long activeFrom) { this.activeFrom = activeFrom; }
    public Long getRetireAfter() { return retireAfter; }
    public void setRetireAfter(Long retireAfter) { this.retireAfter = retireAfter; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}