package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.DomainVerification;

import java.util.Optional;

/**
 * Repository for identity_domain_verification.
 */
public interface DomainVerificationRepository {
    void save(DomainVerification verification);
    Optional<DomainVerification> findById(String id);
    Optional<DomainVerification> findByDomainId(String domainId);
    void update(DomainVerification verification);
    void updateStatus(String id, String status, long now, long version);
}
