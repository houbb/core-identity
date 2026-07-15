package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.Membership;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_membership.
 */
public interface MembershipRepository {

    void save(Membership membership);

    Optional<Membership> findById(String id);

    Optional<Membership> findByOrgAndUser(String organizationId, String userId);

    List<Membership> findByUserId(String userId);

    List<Membership> findByOrganizationId(String organizationId);

    List<Membership> findByOrgAndStatus(String organizationId, String status);

    void update(Membership membership);

    void updateStatus(String id, String status, long now, long version);

    void updateLastAccessed(String id, long now, long version);

    int countActiveByOrgId(String organizationId);
}