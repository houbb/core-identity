package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.AuthenticationChallenge;

import java.util.Optional;

/**
 * Repository for identity_authentication_challenge.
 */
public interface AuthenticationChallengeRepository {

    void save(AuthenticationChallenge challenge);

    Optional<AuthenticationChallenge> findById(String id);

    void updateStatus(String id, String status, Long completedAt, int attemptCount, long version);

    void expireStale(long beforeTimestamp);
}
