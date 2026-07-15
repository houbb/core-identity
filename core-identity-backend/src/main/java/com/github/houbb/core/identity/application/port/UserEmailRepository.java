package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.UserEmail;

import java.util.Optional;

/**
 * Repository for identity_user_email.
 */
public interface UserEmailRepository {

    void save(UserEmail email);

    Optional<UserEmail> findByNormalized(String emailNormalized);

    Optional<UserEmail> findByUserId(String userId);

    void update(UserEmail email);

    void markVerified(String id, long verifiedAt, long version);
}