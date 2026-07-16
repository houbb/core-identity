package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.FederationConnection;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_federation_connection.
 */
public interface FederationConnectionRepository {
    void save(FederationConnection connection);
    Optional<FederationConnection> findById(String id);
    Optional<FederationConnection> findByConnectionKey(String connectionKey);
    List<FederationConnection> findByOrganizationId(String organizationId);
    List<FederationConnection> findByOrganizationIdAndStatus(String organizationId, String status);
    List<FederationConnection> findByStatus(String status);
    void update(FederationConnection connection);
    void updateStatus(String id, String status, long lastFailureAt, String lastErrorCode, long now, long version);
    void updateLastSuccess(String id, long lastSuccessAt, long now, long version);
}
