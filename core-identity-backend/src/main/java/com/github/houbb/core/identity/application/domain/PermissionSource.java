package com.github.houbb.core.identity.application.domain;

/**
 * Permission source — records when a service last synced its permission manifest.
 *
 * Table: identity_permission_source
 */
public class PermissionSource {

    private String id;
    private String serviceName;
    private String manifestVersion;
    private String checksum;
    private long lastSyncedAt;
    private String lastSyncedBy;
    private String status;
    private long createdAt;
    private long updatedAt;
    private long version;

    public PermissionSource() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getManifestVersion() { return manifestVersion; }
    public void setManifestVersion(String manifestVersion) { this.manifestVersion = manifestVersion; }
    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }
    public long getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(long lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }
    public String getLastSyncedBy() { return lastSyncedBy; }
    public void setLastSyncedBy(String lastSyncedBy) { this.lastSyncedBy = lastSyncedBy; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}