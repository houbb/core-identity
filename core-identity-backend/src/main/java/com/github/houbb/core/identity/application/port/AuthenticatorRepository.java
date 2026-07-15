package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.Authenticator;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_authenticator.
 */
public interface AuthenticatorRepository {

    void save(Authenticator authenticator);

    Optional<Authenticator> findById(String id);

    List<Authenticator> findByUserId(String userId);

    List<Authenticator> findByUserIdAndStatus(String userId, String status);

    Optional<Authenticator> findByUserIdAndType(String userId, String authenticatorType);

    void update(Authenticator authenticator);

    void updateStatus(String id, String status, long version);

    void updateLastUsedAt(String id, long lastUsedAt, long version);

    int countActiveByUserIdAndType(String userId, String authenticatorType);
}
