package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.StepUpGrant;

import java.util.Optional;

/**
 * Repository for identity_step_up_grant.
 */
public interface StepUpGrantRepository {

    void save(StepUpGrant grant);

    Optional<StepUpGrant> findById(String id);

    Optional<StepUpGrant> findActiveByUserIdAndSession(String userId, String sessionId, long now);

    void consume(String id, long consumedAt, long version);

    void expireStale(long beforeTimestamp);
}
