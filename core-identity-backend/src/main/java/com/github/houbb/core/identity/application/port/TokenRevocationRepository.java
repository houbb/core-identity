package com.github.houbb.core.identity.application.port;

/**
 * Repository for identity_token_revocation (emergency token revocation table).
 */
public interface TokenRevocationRepository {

    void save(String jti, String subjectId, String reason, long expiresAt);

    boolean isRevoked(String jti);

    void deleteExpired(long beforeTimestamp);
}