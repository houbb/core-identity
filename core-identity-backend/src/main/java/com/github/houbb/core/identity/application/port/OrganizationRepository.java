package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.Organization;

import java.util.Optional;

/**
 * Repository for identity_organization.
 */
public interface OrganizationRepository {

    void save(Organization organization);

    Optional<Organization> findById(String id);

    Optional<Organization> findByPersonalOwner(String userId);

    Optional<Organization> findBySlug(String slug);

    void update(Organization organization);
}