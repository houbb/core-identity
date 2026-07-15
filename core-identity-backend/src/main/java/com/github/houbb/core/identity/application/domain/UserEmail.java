package com.github.houbb.core.identity.application.domain;

/**
 * User email domain object.
 */
public class UserEmail {

    private String id;
    private String userId;
    private String emailNormalized;
    private String emailDisplay;
    private int isPrimary;
    private Long verifiedAt;
    private long createdAt;
    private long updatedAt;
    private long version;

    public UserEmail() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEmailNormalized() { return emailNormalized; }
    public void setEmailNormalized(String emailNormalized) { this.emailNormalized = emailNormalized; }
    public String getEmailDisplay() { return emailDisplay; }
    public void setEmailDisplay(String emailDisplay) { this.emailDisplay = emailDisplay; }
    public int getIsPrimary() { return isPrimary; }
    public void setIsPrimary(int isPrimary) { this.isPrimary = isPrimary; }
    public Long getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Long verifiedAt) { this.verifiedAt = verifiedAt; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}