package com.coreplatform.identity.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "credential")
public class Credential extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "credential_type", nullable = false, length = 32)
    private CredentialType credentialType;

    @Column(name = "credential_value", nullable = false, length = 512)
    private String credentialValue;

    @Column(name = "expire_time")
    private java.time.LocalDateTime expireTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CredentialStatus status = CredentialStatus.ACTIVE;

    public enum CredentialType {
        PASSWORD, API_KEY, TOTP, RECOVERY_CODE
    }

    public enum CredentialStatus {
        ACTIVE, EXPIRED, REVOKED
    }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    public CredentialType getCredentialType() { return credentialType; }
    public void setCredentialType(CredentialType credentialType) { this.credentialType = credentialType; }
    public String getCredentialValue() { return credentialValue; }
    public void setCredentialValue(String credentialValue) { this.credentialValue = credentialValue; }
    public java.time.LocalDateTime getExpireTime() { return expireTime; }
    public void setExpireTime(java.time.LocalDateTime expireTime) { this.expireTime = expireTime; }
    public CredentialStatus getStatus() { return status; }
    public void setStatus(CredentialStatus status) { this.status = status; }
}