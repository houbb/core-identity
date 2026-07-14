package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.InstanceMetadata;

import java.util.Optional;

/**
 * Repository for instance metadata.
 */
public interface InstanceMetadataRepository {

    void save(InstanceMetadata metadata);

    Optional<InstanceMetadata> findById(String instanceId);

    void update(InstanceMetadata metadata);
}