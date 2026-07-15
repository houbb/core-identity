package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.OneTimeToken;

import java.util.Optional;

/**
 * Repository for identity_one_time_token.
 */
public interface OneTimeTokenRepository {

    void save(OneTimeToken token);

    Optional<OneTimeToken> findByTokenHash(String tokenHash);

    Optional<OneTimeToken> findActiveByUserAndType(String userId, String tokenType);

    void markUsed(String id, long usedAt, long version);

    void revokeAllForUser(String userId);

    void revokeAllForUserAndType(String userId, String tokenType);

    void expireBefore(long timestamp);
}