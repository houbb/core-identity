package com.github.houbb.core.identity.application.domain;

/**
 * SCIM Resource domain object — maps SCIM external resources to local Core Identity resources.
 *
 * P5: Tracks provisioned Users/Groups with versioning for ETag concurrency control.
 * Unique key: connectionId + resourceType + externalId.
 * Table: identity_scim_resource
 */
public class ScimResource {

    private String id;
    private String connectionId;
    private String resourceType;
    private String externalId;
    private String localResourceId;
    private String userName;
    private int active;
    private long resourceVersion;
    private String lastPayloadHash;
    private Long lastSyncedAt;
    private long createdAt;
    private long updatedAt;
    private long version;

    public ScimResource() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getConnectionId() { return connectionId; }
    public void setConnectionId(String connectionId) { this.connectionId = connectionId; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public String getLocalResourceId() { return localResourceId; }
    public void setLocalResourceId(String localResourceId) { this.localResourceId = localResourceId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public int getActive() { return active; }
    public void setActive(int active) { this.active = active; }
    public long getResourceVersion() { return resourceVersion; }
    public void setResourceVersion(long resourceVersion) { this.resourceVersion = resourceVersion; }
    public String getLastPayloadHash() { return lastPayloadHash; }
    public void setLastPayloadHash(String lastPayloadHash) { this.lastPayloadHash = lastPayloadHash; }
    public Long getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(Long lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
