package com.github.houbb.core.identity.infrastructure.task;

import com.github.houbb.core.identity.application.domain.RuntimeLease;
import com.github.houbb.core.identity.application.port.LeaderElectionPort;
import com.github.houbb.core.identity.application.port.RuntimeLeaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;

/**
 * Database-based leader election (P7.4).
 * <p>
 * Uses the identity_runtime_lease table with optimistic locking and
 * fencing tokens to coordinate singleton task execution across nodes.
 * <p>
 * No Kubernetes, no ZooKeeper, no Redis — just database locks.
 */
public class DatabaseLeaderElection implements LeaderElectionPort {

    private static final Logger log = LoggerFactory.getLogger(DatabaseLeaderElection.class);

    private final RuntimeLeaseRepository repository;

    public DatabaseLeaderElection(RuntimeLeaseRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean tryAcquireLease(String leaseName, String nodeId, Duration ttl) {
        long now = System.currentTimeMillis();
        long lockedUntil = now + ttl.toMillis();

        // Try to insert or update the lease
        Optional<RuntimeLease> existing = repository.findByLeaseName(leaseName);

        if (existing.isPresent()) {
            RuntimeLease lease = existing.get();
            // If the lease is expired or owned by us, we can try to acquire
            if (lease.getOwnerNodeId().equals(nodeId) || lease.getLockedUntil() < now) {
                long newFencingToken = lease.getFencingToken() + 1;
                boolean acquired = repository.tryAcquire(leaseName, nodeId, now, lockedUntil, newFencingToken);
                if (acquired) {
                    log.debug("Lease '{}' acquired by node {} (fencing={})", leaseName, nodeId, newFencingToken);
                }
                return acquired;
            }
            // Lease is still held by another node
            log.debug("Lease '{}' held by node {}, locked until {}", leaseName, lease.getOwnerNodeId(), lease.getLockedUntil());
            return false;
        } else {
            // No lease exists yet — create one
            RuntimeLease newLease = new RuntimeLease();
            newLease.setLeaseName(leaseName);
            newLease.setOwnerNodeId(nodeId);
            newLease.setFencingToken(1L);
            newLease.setAcquiredAt(now);
            newLease.setHeartbeatAt(now);
            newLease.setLockedUntil(lockedUntil);
            newLease.setVersion(1L);

            repository.insertOrUpdate(newLease);
            log.info("Lease '{}' created and acquired by node {} (fencing=1)", leaseName, nodeId);
            return true;
        }
    }

    @Override
    public boolean renewLease(String leaseName, String nodeId) {
        Optional<RuntimeLease> existing = repository.findByLeaseName(leaseName);
        if (existing.isEmpty()) {
            return false;
        }
        RuntimeLease lease = existing.get();
        if (!lease.getOwnerNodeId().equals(nodeId)) {
            return false;
        }

        long now = System.currentTimeMillis();
        long lockedUntil = now + 30_000L; // extend by 30 seconds
        boolean renewed = repository.renew(leaseName, nodeId, now, lockedUntil, lease.getFencingToken());
        if (renewed) {
            log.debug("Lease '{}' renewed by node {}", leaseName, nodeId);
        }
        return renewed;
    }

    @Override
    public void releaseLease(String leaseName, String nodeId) {
        repository.release(leaseName, nodeId);
        log.info("Lease '{}' released by node {}", leaseName, nodeId);
    }

    @Override
    public Optional<String> getCurrentLeader(String leaseName) {
        return repository.findByLeaseName(leaseName)
                .filter(lease -> lease.getLockedUntil() > System.currentTimeMillis())
                .map(RuntimeLease::getOwnerNodeId);
    }

    @Override
    public Optional<Long> getFencingToken(String leaseName) {
        return repository.findByLeaseName(leaseName)
                .map(RuntimeLease::getFencingToken);
    }
}