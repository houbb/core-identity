package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.AccessGrant;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_access_grant.
 */
public interface AccessGrantRepository {

    void save(AccessGrant grant);

    Optional<AccessGrant> findById(String id);

    List<AccessGrant> findBySubjectId(String subjectId);

    List<AccessGrant> findBySubjectIdAndOrg(String subjectId, String organizationId);

    List<AccessGrant> findByEntitlementId(String entitlementId);

    List<AccessGrant> findActiveBySubjectId(String subjectId);

    List<AccessGrant> findExpiringGrants(long beforeTimestamp, String status);

    List<AccessGrant> findBySourceTypeAndSourceId(String sourceType, String sourceId);

    void update(AccessGrant grant);

    void updateStatus(String id, String status, long now, long version);

    void updateStatusAndExpiry(String id, String status, long expiresAt, long now, long version);

    void revoke(String id, String revokedBy, long revokedAt, String reason, long now, long version);

    int countActiveBySubjectId(String subjectId);
}
