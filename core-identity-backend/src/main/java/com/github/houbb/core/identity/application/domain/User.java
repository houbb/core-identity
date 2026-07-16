package com.github.houbb.core.identity.application.domain;

/**
 * User domain object — the core identity principal.
 *
 * P4 extended fields: securityVersion, securityStatus, riskLevel, mfaEnrolled,
 * phishingResistantEnrolled, recoveryState, lastSecurityReviewAt.
 *
 * P5 extended fields: primaryIdentitySource.
 */
public class User {

    private String id;
    private String displayName;
    private String status;
    private String locale;
    private String timezone;
    private String avatarObjectId;
    private Long lastLoginAt;
    private Long lockedUntil;
    private Long disabledAt;
    private String disabledReason;
    // P4 security fields
    private long securityVersion;
    private String securityStatus;
    private String riskLevel;
    private int mfaEnrolled;
    private int phishingResistantEnrolled;
    private String recoveryState;
    private Long lastSecurityReviewAt;
    private String primaryIdentitySource;
    private long createdAt;
    private long updatedAt;
    private long version;

    public User() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public String getAvatarObjectId() { return avatarObjectId; }
    public void setAvatarObjectId(String avatarObjectId) { this.avatarObjectId = avatarObjectId; }
    public Long getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(Long lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public Long getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(Long lockedUntil) { this.lockedUntil = lockedUntil; }
    public Long getDisabledAt() { return disabledAt; }
    public void setDisabledAt(Long disabledAt) { this.disabledAt = disabledAt; }
    public String getDisabledReason() { return disabledReason; }
    public void setDisabledReason(String disabledReason) { this.disabledReason = disabledReason; }
    public long getSecurityVersion() { return securityVersion; }
    public void setSecurityVersion(long securityVersion) { this.securityVersion = securityVersion; }
    public String getSecurityStatus() { return securityStatus; }
    public void setSecurityStatus(String securityStatus) { this.securityStatus = securityStatus; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public int getMfaEnrolled() { return mfaEnrolled; }
    public void setMfaEnrolled(int mfaEnrolled) { this.mfaEnrolled = mfaEnrolled; }
    public int getPhishingResistantEnrolled() { return phishingResistantEnrolled; }
    public void setPhishingResistantEnrolled(int phishingResistantEnrolled) { this.phishingResistantEnrolled = phishingResistantEnrolled; }
    public String getRecoveryState() { return recoveryState; }
    public void setRecoveryState(String recoveryState) { this.recoveryState = recoveryState; }
    public Long getLastSecurityReviewAt() { return lastSecurityReviewAt; }
    public void setLastSecurityReviewAt(Long lastSecurityReviewAt) { this.lastSecurityReviewAt = lastSecurityReviewAt; }
    public String getPrimaryIdentitySource() { return primaryIdentitySource; }
    public void setPrimaryIdentitySource(String primaryIdentitySource) { this.primaryIdentitySource = primaryIdentitySource; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}