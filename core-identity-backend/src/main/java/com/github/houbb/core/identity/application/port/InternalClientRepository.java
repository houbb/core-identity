package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.InternalClient;

import java.util.Optional;

/**
 * Repository for internal clients.
 */
public interface InternalClientRepository {

    void save(InternalClient client);

    Optional<InternalClient> findByClientId(String clientId);

    void updateLastUsedAt(String clientId, long timestamp);
}