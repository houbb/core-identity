package com.github.houbb.core.identity.application.domain;

/**
 * Security policy domain object — organization-level security requirements.
 */
public class SecurityPolicy {

    private String id;
    private String organizationId;
    private String name;
    private String status;
    private String minimumAuthLevel;
    private int phishingResistantRequired;
    private String allowedAuthenticatorTypesJson;
    private int privilegedRolesOnly;
    private Integer trustedDeviceDays;
    private Integer sessionIdleSeconds;
    private Integer sessionAbsoluteSeconds;
    private Integer reauthSeconds;
    private Long gracePeriodEndsAt;
    private String createdBy;
    private Long publishedAt;
    private long createdAt;
    private long updatedAt;
    private long version;

    public SecurityPolicy() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMinimumAuthLevel() { return minimumAuthLevel; }
    public void setMinimumAuthLevel(String minimumAuthLevel) { this.minimumAuthLevel = minimumAuthLevel; }
    public int getPhishingResistantRequired() { return phishingResistantRequired; }
    public void setPhishingResistantRequired(int phishingResistantRequired) { this.phishingResistantRequired = phishingResistantRequired; }
    public String getAllowedAuthenticatorTypesJson() { return allowedAuthenticatorTypesJson; }
    public void setAllowedAuthenticatorTypesJson(String allowedAuthenticatorTypesJson) { this.allowedAuthenticatorTypesJson = allowedAuthenticatorTypesJson; }
    public int getPrivilegedRolesOnly() { return privilegedRolesOnly; }
    public void setPrivilegedRolesOnly(int privilegedRolesOnly) { this.privilegedRolesOnly = privilegedRolesOnly; }
    public Integer getTrustedDeviceDays() { return trustedDeviceDays; }
    public void setTrustedDeviceDays(Integer trustedDeviceDays) { this.trustedDeviceDays = trustedDeviceDays; }
    public Integer getSessionIdleSeconds() { return sessionIdleSeconds; }
    public void setSessionIdleSeconds(Integer sessionIdleSeconds) { this.sessionIdleSeconds = sessionIdleSeconds; }
    public Integer getSessionAbsoluteSeconds() { return sessionAbsoluteSeconds; }
    public void setSessionAbsoluteSeconds(Integer sessionAbsoluteSeconds) { this.sessionAbsoluteSeconds = sessionAbsoluteSeconds; }
    public Integer getReauthSeconds() { return reauthSeconds; }
    public void setReauthSeconds(Integer reauthSeconds) { this.reauthSeconds = reauthSeconds; }
    public Long getGracePeriodEndsAt() { return gracePeriodEndsAt; }
    public void setGracePeriodEndsAt(Long gracePeriodEndsAt) { this.gracePeriodEndsAt = gracePeriodEndsAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Long getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Long publishedAt) { this.publishedAt = publishedAt; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
