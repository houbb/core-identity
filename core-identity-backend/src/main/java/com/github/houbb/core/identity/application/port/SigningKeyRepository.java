package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.SigningKey;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_signing_key.
 */
public interface SigningKeyRepository {

    void save(SigningKey key);

    Optional<SigningKey> findById(String id);

    Optional<SigningKey> findByKeyId(String keyId);

    List<SigningKey> findByStatus(String status);

    List<SigningKey> findAllActive();

    List<SigningKey> findAll();

    void update(SigningKey key);
}