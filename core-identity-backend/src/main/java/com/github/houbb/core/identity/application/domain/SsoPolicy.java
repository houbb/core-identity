package com.github.houbb.core.identity.application.domain;

/**
 * SsoPolicy domain object — per-organization SSO enforcement rules.
 *
 * P5: Controls whether members must use enterprise SSO.
 * Supports grace periods and break-glass accounts.
 * Table: identity_sso_policy
 */
public class SsoPolicy {

    private String id;
    private String organizationId;
    private String enforcementMode;
    private String connectionIdsJson;
    private Long gracePeriodEndsAt;
    private int localLoginAllowed;
    private int requireSsoForPrivileged;
    private int breakGlassRequired;
    private String status;
    private Long publishedAt;
    private String createdBy;
    private long createdAt;
    private long updatedAt;
    private long version;

    public SsoPolicy() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    public String getEnforcementMode() { return enforcementMode; }
    public void setEnforcementMode(String enforcementMode) { this.enforcementMode = enforcementMode; }
    public String getConnectionIdsJson() { return connectionIdsJson; }
    public void setConnectionIdsJson(String connectionIdsJson) { this.connectionIdsJson = connectionIdsJson; }
    public Long getGracePeriodEndsAt() { return gracePeriodEndsAt; }
    public void setGracePeriodEndsAt(Long gracePeriodEndsAt) { this.gracePeriodEndsAt = gracePeriodEndsAt; }
    public int getLocalLoginAllowed() { return localLoginAllowed; }
    public void setLocalLoginAllowed(int localLoginAllowed) { this.localLoginAllowed = localLoginAllowed; }
    public int getRequireSsoForPrivileged() { return requireSsoForPrivileged; }
    public void setRequireSsoForPrivileged(int requireSsoForPrivileged) { this.requireSsoForPrivileged = requireSsoForPrivileged; }
    public int getBreakGlassRequired() { return breakGlassRequired; }
    public void setBreakGlassRequired(int breakGlassRequired) { this.breakGlassRequired = breakGlassRequired; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Long publishedAt) { this.publishedAt = publishedAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
