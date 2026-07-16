package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.SamlConnection;

import java.util.Optional;

/**
 * Repository for identity_saml_connection.
 */
public interface SamlConnectionRepository {
    void save(SamlConnection connection);
    Optional<SamlConnection> findById(String connectionId);
    void update(SamlConnection connection);
}
