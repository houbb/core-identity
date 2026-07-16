package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.VerifiedDomain;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_verified_domain.
 */
public interface VerifiedDomainRepository {
    void save(VerifiedDomain domain);
    Optional<VerifiedDomain> findById(String id);
    Optional<VerifiedDomain> findByDomainName(String domainName);
    List<VerifiedDomain> findByOrganizationId(String organizationId);
    List<VerifiedDomain> findByStatus(String status);
    void update(VerifiedDomain domain);
    void updateStatus(String id, String status, long now, long version);
}
