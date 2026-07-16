package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.DistributedJob;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_distributed_job (P7.4).
 */
public interface DistributedJobRepository {

    void save(DistributedJob job);

    Optional<DistributedJob> findById(String id);

    Optional<DistributedJob> findByJobTypeAndKey(String jobType, String jobKey);

    List<DistributedJob> findPending(int limit);

    void updateStatus(String id, String status, long updatedAt, long version);

    boolean tryLock(String id, String nodeId, long fencingToken, long lockedUntil, int attemptCount, long version);

    void complete(String id, String status, long completedAt, long version);

    void deleteExpired(long beforeTimestamp);
}
