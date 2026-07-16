package com.github.houbb.core.identity.application.domain;

/**
 * Cluster node domain object (P7.1).
 * <p>
 * Represents a running instance of the Identity Backend in a cluster.
 * Nodes register at startup, heartbeat periodically, and are marked
 * UNAVAILABLE when they stop heartbeating.
 */
public class ClusterNode {

    private String id;
    private String instanceId;
    private String serviceType;    // IDENTITY, ADMIN, WORKER
    private String version;        // application version (e.g. "0.8.0")
    private String apiVersion;     // API version (e.g. "v1")
    private String region;
    private String availabilityZone;
    private String hostname;
    private String status;         // HEALTHY, DEGRADED, DRAINING, UNAVAILABLE, INCOMPATIBLE
    private long startedAt;
    private long lastHeartbeatAt;
    private Long drainingAt;
    private String metadataJson;   // non-sensitive metadata
    private long createdAt;
    private long updatedAt;
    private long dbVersion;        // optimistic locking version column

    public ClusterNode() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getInstanceId() { return instanceId; }
    public void setInstanceId(String instanceId) { this.instanceId = instanceId; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getApiVersion() { return apiVersion; }
    public void setApiVersion(String apiVersion) { this.apiVersion = apiVersion; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getAvailabilityZone() { return availabilityZone; }
    public void setAvailabilityZone(String availabilityZone) { this.availabilityZone = availabilityZone; }

    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getStartedAt() { return startedAt; }
    public void setStartedAt(long startedAt) { this.startedAt = startedAt; }

    public long getLastHeartbeatAt() { return lastHeartbeatAt; }
    public void setLastHeartbeatAt(long lastHeartbeatAt) { this.lastHeartbeatAt = lastHeartbeatAt; }

    public Long getDrainingAt() { return drainingAt; }
    public void setDrainingAt(Long drainingAt) { this.drainingAt = drainingAt; }

    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    /** Optimistic locking version column (DB). */
    public long getDbVersion() { return dbVersion; }
    public void setDbVersion(long dbVersion) { this.dbVersion = dbVersion; }
}
