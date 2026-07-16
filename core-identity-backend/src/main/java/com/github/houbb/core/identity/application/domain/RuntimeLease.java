package com.github.houbb.core.identity.application.domain;

/**
 * Runtime lease for leader election (P7.4).
 * <p>
 * Used to coordinate which node runs singleton tasks
 * (outbox relay, session expiry, certificate scanning, etc.).
 */
public class RuntimeLease {

    private String leaseName;
    private String ownerNodeId;
    private long fencingToken;
    private long acquiredAt;
    private long heartbeatAt;
    private long lockedUntil;
    private String payloadJson;
    private long version;

    public RuntimeLease() {
    }

    public String getLeaseName() { return leaseName; }
    public void setLeaseName(String leaseName) { this.leaseName = leaseName; }

    public String getOwnerNodeId() { return ownerNodeId; }
    public void setOwnerNodeId(String ownerNodeId) { this.ownerNodeId = ownerNodeId; }

    public long getFencingToken() { return fencingToken; }
    public void setFencingToken(long fencingToken) { this.fencingToken = fencingToken; }

    public long getAcquiredAt() { return acquiredAt; }
    public void setAcquiredAt(long acquiredAt) { this.acquiredAt = acquiredAt; }

    public long getHeartbeatAt() { return heartbeatAt; }
    public void setHeartbeatAt(long heartbeatAt) { this.heartbeatAt = heartbeatAt; }

    public long getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(long lockedUntil) { this.lockedUntil = lockedUntil; }

    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }

    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
