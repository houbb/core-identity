package com.github.houbb.core.identity.application.domain;

/**
 * Authenticator domain object — unified model for all authentication methods.
 *
 * P4: Represents PASSWORD, TOTP, WEBAUTHN, RECOVERY_CODE_SET, EMAIL_RECOVERY.
 */
public class Authenticator {

    private String id;
    private String userId;
    private String authenticatorType;
    private String name;
    private String status;
    private String assuranceLevel;
    private int phishingResistant;
    private int userVerificationCapable;
    private Long enrolledAt;
    private Long lastUsedAt;
    private Long compromisedAt;
    private Long revokedAt;
    private long createdAt;
    private long updatedAt;
    private long version;

    public Authenticator() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getAuthenticatorType() { return authenticatorType; }
    public void setAuthenticatorType(String authenticatorType) { this.authenticatorType = authenticatorType; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAssuranceLevel() { return assuranceLevel; }
    public void setAssuranceLevel(String assuranceLevel) { this.assuranceLevel = assuranceLevel; }
    public int getPhishingResistant() { return phishingResistant; }
    public void setPhishingResistant(int phishingResistant) { this.phishingResistant = phishingResistant; }
    public int getUserVerificationCapable() { return userVerificationCapable; }
    public void setUserVerificationCapable(int userVerificationCapable) { this.userVerificationCapable = userVerificationCapable; }
    public Long getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(Long enrolledAt) { this.enrolledAt = enrolledAt; }
    public Long getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(Long lastUsedAt) { this.lastUsedAt = lastUsedAt; }
    public Long getCompromisedAt() { return compromisedAt; }
    public void setCompromisedAt(Long compromisedAt) { this.compromisedAt = compromisedAt; }
    public Long getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Long revokedAt) { this.revokedAt = revokedAt; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
