package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.SsoPolicy;

import java.util.Optional;

/**
 * Repository for identity_sso_policy.
 */
public interface SsoPolicyRepository {
    void save(SsoPolicy policy);
    Optional<SsoPolicy> findById(String id);
    Optional<SsoPolicy> findByOrganizationId(String organizationId);
    void update(SsoPolicy policy);
}
