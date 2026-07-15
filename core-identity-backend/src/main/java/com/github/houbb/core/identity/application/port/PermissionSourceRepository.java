package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.PermissionSource;

import java.util.Optional;

/**
 * Repository for identity_permission_source.
 */
public interface PermissionSourceRepository {

    void save(PermissionSource source);

    Optional<PermissionSource> findByServiceName(String serviceName);

    void update(PermissionSource source);

    void updateSyncInfo(String id, String manifestVersion, String checksum, long syncedAt, String syncedBy, long version);
}