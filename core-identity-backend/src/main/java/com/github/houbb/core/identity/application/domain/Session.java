package com.github.houbb.core.identity.application.domain;

/**
 * Session domain object — user and admin login sessions.
 *
 * P2 extended fields: lastOrganizationId, permissionVersion.
 */
public class Session {

    private String id;
    private String userId;
    private String sessionType;
    private String tokenHash;
    private String status;
    private String ipAddress;
    private String userAgent;
    private String deviceName;
    private long lastActiveAt;
    private long idleExpiresAt;
    private long absoluteExpiresAt;
    private Long revokedAt;
    private String revokeReason;
    private String lastOrganizationId;
    private long permissionVersion;
    private long createdAt;
    private long updatedAt;
    private long version;

    public Session() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public long getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(long lastActiveAt) { this.lastActiveAt = lastActiveAt; }
    public long getIdleExpiresAt() { return idleExpiresAt; }
    public void setIdleExpiresAt(long idleExpiresAt) { this.idleExpiresAt = idleExpiresAt; }
    public long getAbsoluteExpiresAt() { return absoluteExpiresAt; }
    public void setAbsoluteExpiresAt(long absoluteExpiresAt) { this.absoluteExpiresAt = absoluteExpiresAt; }
    public Long getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Long revokedAt) { this.revokedAt = revokedAt; }
    public String getRevokeReason() { return revokeReason; }
    public void setRevokeReason(String revokeReason) { this.revokeReason = revokeReason; }
    public String getLastOrganizationId() { return lastOrganizationId; }
    public void setLastOrganizationId(String lastOrganizationId) { this.lastOrganizationId = lastOrganizationId; }
    public long getPermissionVersion() { return permissionVersion; }
    public void setPermissionVersion(long permissionVersion) { this.permissionVersion = permissionVersion; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}