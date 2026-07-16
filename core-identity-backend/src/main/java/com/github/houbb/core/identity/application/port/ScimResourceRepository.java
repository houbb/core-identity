package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.ScimResource;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_scim_resource.
 */
public interface ScimResourceRepository {
    void save(ScimResource resource);
    Optional<ScimResource> findById(String id);
    Optional<ScimResource> findByConnectionIdAndResourceTypeAndExternalId(String connectionId, String resourceType, String externalId);
    Optional<ScimResource> findByConnectionIdAndResourceTypeAndLocalResourceId(String connectionId, String resourceType, String localResourceId);
    List<ScimResource> findByConnectionIdAndResourceType(String connectionId, String resourceType);
    void update(ScimResource resource);
    void updateActive(String id, int active, long resourceVersion, long lastSyncedAt, long now, long version);
}
