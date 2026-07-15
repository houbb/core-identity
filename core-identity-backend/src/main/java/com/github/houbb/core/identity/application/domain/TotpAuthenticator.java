package com.github.houbb.core.identity.application.domain;

/**
 * TOTP authenticator domain object — stores encrypted TOTP secret.
 */
public class TotpAuthenticator {

    private String authenticatorId;
    private String encryptedSecret;
    private String encryptionKeyVersion;
    private String algorithm;
    private int digits;
    private int periodSeconds;
    private long lastAcceptedStep;
    private Long confirmedAt;

    public TotpAuthenticator() {}

    public String getAuthenticatorId() { return authenticatorId; }
    public void setAuthenticatorId(String authenticatorId) { this.authenticatorId = authenticatorId; }
    public String getEncryptedSecret() { return encryptedSecret; }
    public void setEncryptedSecret(String encryptedSecret) { this.encryptedSecret = encryptedSecret; }
    public String getEncryptionKeyVersion() { return encryptionKeyVersion; }
    public void setEncryptionKeyVersion(String encryptionKeyVersion) { this.encryptionKeyVersion = encryptionKeyVersion; }
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
    public int getDigits() { return digits; }
    public void setDigits(int digits) { this.digits = digits; }
    public int getPeriodSeconds() { return periodSeconds; }
    public void setPeriodSeconds(int periodSeconds) { this.periodSeconds = periodSeconds; }
    public long getLastAcceptedStep() { return lastAcceptedStep; }
    public void setLastAcceptedStep(long lastAcceptedStep) { this.lastAcceptedStep = lastAcceptedStep; }
    public Long getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(Long confirmedAt) { this.confirmedAt = confirmedAt; }
}
