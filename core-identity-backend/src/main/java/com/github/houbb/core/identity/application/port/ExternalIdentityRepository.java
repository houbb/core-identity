package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.ExternalIdentity;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_external_identity.
 */
public interface ExternalIdentityRepository {
    void save(ExternalIdentity identity);
    Optional<ExternalIdentity> findById(String id);
    Optional<ExternalIdentity> findByConnectionIdAndExternalSubject(String connectionId, String externalSubject);
    List<ExternalIdentity> findByUserId(String userId);
    List<ExternalIdentity> findByOrganizationId(String organizationId);
    List<ExternalIdentity> findByConnectionId(String connectionId);
    void update(ExternalIdentity identity);
    void updateStatus(String id, String status, long now, long version);
    void updateLastLogin(String id, long lastLoginAt, long now, long version);
    void unlink(String id, long unlinkedAt, long now, long version);
}
