package com.github.houbb.core.identity.application.domain;

/**
 * Session domain object — user and admin login sessions.
 *
 * P2 extended fields: lastOrganizationId, permissionVersion.
 * P4 extended fields: deviceId, authenticationLevel, authenticationMethodsJson,
 * strongAuthAt, riskLevel, reauthRequiredAt, securityVersion, lastRiskEvaluatedAt.
 *
 * P5 extended fields: authenticationSource, federationConnectionId, externalIdentityId.
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
    // P4 security fields
    private String deviceId;
    private String authenticationLevel;
    private String authenticationMethodsJson;
    private Long strongAuthAt;
    private String riskLevel;
    private Long reauthRequiredAt;
    private long securityVersion;
    private Long lastRiskEvaluatedAt;
    private String authenticationSource;
    private String federationConnectionId;
    private String externalIdentityId;
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
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getAuthenticationLevel() { return authenticationLevel; }
    public void setAuthenticationLevel(String authenticationLevel) { this.authenticationLevel = authenticationLevel; }
    public String getAuthenticationMethodsJson() { return authenticationMethodsJson; }
    public void setAuthenticationMethodsJson(String authenticationMethodsJson) { this.authenticationMethodsJson = authenticationMethodsJson; }
    public Long getStrongAuthAt() { return strongAuthAt; }
    public void setStrongAuthAt(Long strongAuthAt) { this.strongAuthAt = strongAuthAt; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public Long getReauthRequiredAt() { return reauthRequiredAt; }
    public void setReauthRequiredAt(Long reauthRequiredAt) { this.reauthRequiredAt = reauthRequiredAt; }
    public long getSecurityVersion() { return securityVersion; }
    public void setSecurityVersion(long securityVersion) { this.securityVersion = securityVersion; }
    public Long getLastRiskEvaluatedAt() { return lastRiskEvaluatedAt; }
    public void setLastRiskEvaluatedAt(Long lastRiskEvaluatedAt) { this.lastRiskEvaluatedAt = lastRiskEvaluatedAt; }
    public String getAuthenticationSource() { return authenticationSource; }
    public void setAuthenticationSource(String authenticationSource) { this.authenticationSource = authenticationSource; }
    public String getFederationConnectionId() { return federationConnectionId; }
    public void setFederationConnectionId(String federationConnectionId) { this.federationConnectionId = federationConnectionId; }
    public String getExternalIdentityId() { return externalIdentityId; }
    public void setExternalIdentityId(String externalIdentityId) { this.externalIdentityId = externalIdentityId; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}