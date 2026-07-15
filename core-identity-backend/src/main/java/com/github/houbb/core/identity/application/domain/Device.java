package com.github.houbb.core.identity.application.domain;

/**
 * Device domain object — identifies user devices for risk assessment.
 */
public class Device {

    private String id;
    private String userId;
    private String deviceCookieHash;
    private String displayName;
    private String browser;
    private String operatingSystem;
    private long firstSeenAt;
    private long lastSeenAt;
    private String lastIp;
    private String status;
    private long createdAt;
    private long updatedAt;
    private long version;

    public Device() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getDeviceCookieHash() { return deviceCookieHash; }
    public void setDeviceCookieHash(String deviceCookieHash) { this.deviceCookieHash = deviceCookieHash; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }
    public String getOperatingSystem() { return operatingSystem; }
    public void setOperatingSystem(String operatingSystem) { this.operatingSystem = operatingSystem; }
    public long getFirstSeenAt() { return firstSeenAt; }
    public void setFirstSeenAt(long firstSeenAt) { this.firstSeenAt = firstSeenAt; }
    public long getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(long lastSeenAt) { this.lastSeenAt = lastSeenAt; }
    public String getLastIp() { return lastIp; }
    public void setLastIp(String lastIp) { this.lastIp = lastIp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
