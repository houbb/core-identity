package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.Credential;

import java.util.Optional;

/**
 * Repository for identity_credential.
 */
public interface CredentialRepository {

    void save(Credential credential);

    Optional<Credential> findByUserIdAndType(String userId, String credentialType);

    void update(Credential credential);

    void incrementFailedAttempts(String id, int newCount, long version);

    void updatePassword(String id, String secretHash, String algorithm, long passwordChangedAt, long version);

    void revokeByUserId(String userId);
}