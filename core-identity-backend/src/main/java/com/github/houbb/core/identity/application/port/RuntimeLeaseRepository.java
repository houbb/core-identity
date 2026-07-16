package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.RuntimeLease;

import java.util.Optional;

/**
 * Repository for identity_runtime_lease (P7.4).
 */
public interface RuntimeLeaseRepository {

    Optional<RuntimeLease> findByLeaseName(String leaseName);

    void insertOrUpdate(RuntimeLease lease);

    boolean tryAcquire(String leaseName, String nodeId, long now, long lockedUntil, long fencingToken);

    boolean renew(String leaseName, String nodeId, long heartbeatAt, long lockedUntil, long fencingToken);

    void release(String leaseName, String nodeId);
}
