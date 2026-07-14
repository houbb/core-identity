package com.github.houbb.core.identity.application.domain;

/**
 * Instance metadata domain object.
 */
public class InstanceMetadata {

    private String instanceId;
    private String instanceName;
    private String installationId;
    private String edition;
    private String currentVersion;
    private String schemaVersion;
    private long installedAt;
    private long lastStartedAt;
    private long createdAt;
    private long updatedAt;

    public InstanceMetadata() {
    }

    public String getInstanceId() { return instanceId; }
    public void setInstanceId(String instanceId) { this.instanceId = instanceId; }

    public String getInstanceName() { return instanceName; }
    public void setInstanceName(String instanceName) { this.instanceName = instanceName; }

    public String getInstallationId() { return installationId; }
    public void setInstallationId(String installationId) { this.installationId = installationId; }

    public String getEdition() { return edition; }
    public void setEdition(String edition) { this.edition = edition; }

    public String getCurrentVersion() { return currentVersion; }
    public void setCurrentVersion(String currentVersion) { this.currentVersion = currentVersion; }

    public String getSchemaVersion() { return schemaVersion; }
    public void setSchemaVersion(String schemaVersion) { this.schemaVersion = schemaVersion; }

    public long getInstalledAt() { return installedAt; }
    public void setInstalledAt(long installedAt) { this.installedAt = installedAt; }

    public long getLastStartedAt() { return lastStartedAt; }
    public void setLastStartedAt(long lastStartedAt) { this.lastStartedAt = lastStartedAt; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}