package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.OidcConnection;

import java.util.Optional;

/**
 * Repository for identity_oidc_connection.
 */
public interface OidcConnectionRepository {
    void save(OidcConnection connection);
    Optional<OidcConnection> findById(String connectionId);
    void update(OidcConnection connection);
    void updateDiscoveryCache(String connectionId, String configurationCacheJson, long fetchedAt, long now, long version);
}
