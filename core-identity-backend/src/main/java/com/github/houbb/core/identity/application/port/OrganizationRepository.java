package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.Organization;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_organization.
 */
public interface OrganizationRepository {

    void save(Organization organization);

    Optional<Organization> findById(String id);

    Optional<Organization> findByPersonalOwner(String userId);

    Optional<Organization> findByOwnerUserId(String userId);

    List<Organization> findAllByUserId(String userId);

    Optional<Organization> findBySlug(String slug);

    void update(Organization organization);

    void updateOwner(String id, String newOwnerUserId, long authorizationVersion, long now, long version);

    void updateStatus(String id, String status, long now, long version);

    int countByUserIdAndStatus(String userId, String status);
}