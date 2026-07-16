package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.ScimGroup;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_scim_group.
 */
public interface ScimGroupRepository {
    void save(ScimGroup group);
    Optional<ScimGroup> findById(String id);
    Optional<ScimGroup> findByConnectionIdAndExternalId(String connectionId, String externalId);
    List<ScimGroup> findByConnectionId(String connectionId);
    void update(ScimGroup group);
    void updateStatus(String id, String status, long now, long version);
}
