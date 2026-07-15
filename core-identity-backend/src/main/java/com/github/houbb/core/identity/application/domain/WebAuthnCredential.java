package com.github.houbb.core.identity.application.domain;

/**
 * WebAuthn credential domain object — stores public key credentials.
 */
public class WebAuthnCredential {

    private String authenticatorId;
    private String credentialId;
    private String publicKey;
    private String userHandle;
    private long signCount;
    private String aaguid;
    private String transportsJson;
    private String attachment;
    private int discoverable;
    private int backupEligible;
    private int backupState;
    private String attestationFormat;
    private String createdOrigin;
    private String rpId;
    private long createdAt;
    private Long lastUsedAt;

    public WebAuthnCredential() {}

    public String getAuthenticatorId() { return authenticatorId; }
    public void setAuthenticatorId(String authenticatorId) { this.authenticatorId = authenticatorId; }
    public String getCredentialId() { return credentialId; }
    public void setCredentialId(String credentialId) { this.credentialId = credentialId; }
    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
    public String getUserHandle() { return userHandle; }
    public void setUserHandle(String userHandle) { this.userHandle = userHandle; }
    public long getSignCount() { return signCount; }
    public void setSignCount(long signCount) { this.signCount = signCount; }
    public String getAaguid() { return aaguid; }
    public void setAaguid(String aaguid) { this.aaguid = aaguid; }
    public String getTransportsJson() { return transportsJson; }
    public void setTransportsJson(String transportsJson) { this.transportsJson = transportsJson; }
    public String getAttachment() { return attachment; }
    public void setAttachment(String attachment) { this.attachment = attachment; }
    public int getDiscoverable() { return discoverable; }
    public void setDiscoverable(int discoverable) { this.discoverable = discoverable; }
    public int getBackupEligible() { return backupEligible; }
    public void setBackupEligible(int backupEligible) { this.backupEligible = backupEligible; }
    public int getBackupState() { return backupState; }
    public void setBackupState(int backupState) { this.backupState = backupState; }
    public String getAttestationFormat() { return attestationFormat; }
    public void setAttestationFormat(String attestationFormat) { this.attestationFormat = attestationFormat; }
    public String getCreatedOrigin() { return createdOrigin; }
    public void setCreatedOrigin(String createdOrigin) { this.createdOrigin = createdOrigin; }
    public String getRpId() { return rpId; }
    public void setRpId(String rpId) { this.rpId = rpId; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public Long getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(Long lastUsedAt) { this.lastUsedAt = lastUsedAt; }
}
