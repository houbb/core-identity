package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.Membership;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_membership.
 */
public interface MembershipRepository {

    void save(Membership membership);

    Optional<Membership> findByOrgAndUser(String organizationId, String userId);

    List<Membership> findByUserId(String userId);

    List<Membership> findByOrganizationId(String organizationId);

    void update(Membership membership);
}