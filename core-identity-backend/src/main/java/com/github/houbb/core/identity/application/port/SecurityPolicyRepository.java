package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.SecurityPolicy;

import java.util.Optional;

/**
 * Repository for identity_security_policy.
 */
public interface SecurityPolicyRepository {

    Optional<SecurityPolicy> findByOrganizationId(String organizationId);

    void save(SecurityPolicy policy);

    void update(SecurityPolicy policy);
}
