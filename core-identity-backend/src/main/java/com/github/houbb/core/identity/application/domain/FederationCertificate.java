package com.github.houbb.core.identity.application.domain;

/**
 * Federation Certificate domain object — X.509 certificates for SAML signing/encryption and OIDC JWKS.
 *
 * P5: Supports dual-certificate rotation with status lifecycle.
 * Types: IDP_SIGNING, IDP_ENCRYPTION, SP_SIGNING, SP_ENCRYPTION
 * Table: identity_federation_certificate
 */
public class FederationCertificate {

    private String id;
    private String connectionId;
    private String certificateType;
    private String certificatePem;
    private String encryptedPrivateKey;
    private String keyVersion;
    private String fingerprint;
    private String status;
    private Long validFrom;
    private Long validUntil;
    private long createdAt;
    private long updatedAt;
    private long version;

    public FederationCertificate() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getConnectionId() { return connectionId; }
    public void setConnectionId(String connectionId) { this.connectionId = connectionId; }
    public String getCertificateType() { return certificateType; }
    public void setCertificateType(String certificateType) { this.certificateType = certificateType; }
    public String getCertificatePem() { return certificatePem; }
    public void setCertificatePem(String certificatePem) { this.certificatePem = certificatePem; }
    public String getEncryptedPrivateKey() { return encryptedPrivateKey; }
    public void setEncryptedPrivateKey(String encryptedPrivateKey) { this.encryptedPrivateKey = encryptedPrivateKey; }
    public String getKeyVersion() { return keyVersion; }
    public void setKeyVersion(String keyVersion) { this.keyVersion = keyVersion; }
    public String getFingerprint() { return fingerprint; }
    public void setFingerprint(String fingerprint) { this.fingerprint = fingerprint; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getValidFrom() { return validFrom; }
    public void setValidFrom(Long validFrom) { this.validFrom = validFrom; }
    public Long getValidUntil() { return validUntil; }
    public void setValidUntil(Long validUntil) { this.validUntil = validUntil; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
