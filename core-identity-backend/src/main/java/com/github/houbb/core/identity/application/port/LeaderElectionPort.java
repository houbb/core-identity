package com.github.houbb.core.identity.application.port;

import java.time.Duration;
import java.util.Optional;

/**
 * Leader election abstraction (P7.4).
 * <p>
 * Uses database leases (not Kubernetes, not ZooKeeper) to coordinate
 * which node should run singleton tasks.
 * <p>
 * Implementation: {@link com.github.houbb.core.identity.infrastructure.task.DatabaseLeaderElection}
 */
public interface LeaderElectionPort {

    /**
     * Try to acquire a named lease for this node.
     *
     * @param leaseName the lease name (e.g. "outbox-relay", "session-expiry")
     * @param nodeId    the node attempting to acquire the lease
     * @param ttl       how long the lease is valid before renewal is required
     * @return true if the lease was acquired
     */
    boolean tryAcquireLease(String leaseName, String nodeId, Duration ttl);

    /**
     * Renew an existing lease.
     *
     * @param leaseName the lease name
     * @param nodeId    the node renewing
     * @return true if the lease was renewed
     */
    boolean renewLease(String leaseName, String nodeId);

    /**
     * Release a lease held by this node.
     */
    void releaseLease(String leaseName, String nodeId);

    /**
     * Get the current owner of a lease.
     */
    Optional<String> getCurrentLeader(String leaseName);

    /**
     * Get the current fencing token for a lease.
     */
    Optional<Long> getFencingToken(String leaseName);
}
