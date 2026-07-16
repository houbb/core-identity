package com.github.houbb.core.identity.application.domain;

/**
 * SCIM Group domain object — external directory groups synchronized via SCIM.
 *
 * P5: Groups are mapped to local Roles via ScimGroupRoleMapping.
 * Not all external groups map to platform permissions.
 * Table: identity_scim_group
 */
public class ScimGroup {

    private String id;
    private String connectionId;
    private String scimResourceId;
    private String externalId;
    private String displayName;
    private String status;
    private long createdAt;
    private long updatedAt;
    private long version;

    public ScimGroup() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getConnectionId() { return connectionId; }
    public void setConnectionId(String connectionId) { this.connectionId = connectionId; }
    public String getScimResourceId() { return scimResourceId; }
    public void setScimResourceId(String scimResourceId) { this.scimResourceId = scimResourceId; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
