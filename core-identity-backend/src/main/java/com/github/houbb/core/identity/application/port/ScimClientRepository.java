package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.ScimClient;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_scim_client.
 */
public interface ScimClientRepository {
    void save(ScimClient client);
    Optional<ScimClient> findById(String id);
    Optional<ScimClient> findByTokenPrefix(String tokenPrefix);
    List<ScimClient> findByOrganizationId(String organizationId);
    List<ScimClient> findByConnectionId(String connectionId);
    void update(ScimClient client);
    void updateLastUsed(String id, long lastUsedAt, String lastUsedIp, long now, long version);
}
