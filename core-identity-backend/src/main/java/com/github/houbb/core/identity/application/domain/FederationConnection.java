package com.github.houbb.core.identity.application.domain;

/**
 * Federation Connection domain object — enterprise identity provider connection configuration.
 *
 * P5: Core federation model. One organization can have multiple connections (OIDC or SAML).
 * Table: identity_federation_connection
 */
public class FederationConnection {

    private String id;
    private String connectionKey;
    private String organizationId;
    private String connectionType;
    private String name;
    private String status;
    private String loginButtonText;
    private String logoObjectId;
    private int priority;
    private int jitEnabled;
    private int scimEnabled;
    private Long lastSuccessAt;
    private Long lastFailureAt;
    private String lastErrorCode;
    private String createdBy;
    private long createdAt;
    private long updatedAt;
    private long version;

    public FederationConnection() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getConnectionKey() { return connectionKey; }
    public void setConnectionKey(String connectionKey) { this.connectionKey = connectionKey; }
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    public String getConnectionType() { return connectionType; }
    public void setConnectionType(String connectionType) { this.connectionType = connectionType; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLoginButtonText() { return loginButtonText; }
    public void setLoginButtonText(String loginButtonText) { this.loginButtonText = loginButtonText; }
    public String getLogoObjectId() { return logoObjectId; }
    public void setLogoObjectId(String logoObjectId) { this.logoObjectId = logoObjectId; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public int getJitEnabled() { return jitEnabled; }
    public void setJitEnabled(int jitEnabled) { this.jitEnabled = jitEnabled; }
    public int getScimEnabled() { return scimEnabled; }
    public void setScimEnabled(int scimEnabled) { this.scimEnabled = scimEnabled; }
    public Long getLastSuccessAt() { return lastSuccessAt; }
    public void setLastSuccessAt(Long lastSuccessAt) { this.lastSuccessAt = lastSuccessAt; }
    public Long getLastFailureAt() { return lastFailureAt; }
    public void setLastFailureAt(Long lastFailureAt) { this.lastFailureAt = lastFailureAt; }
    public String getLastErrorCode() { return lastErrorCode; }
    public void setLastErrorCode(String lastErrorCode) { this.lastErrorCode = lastErrorCode; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
