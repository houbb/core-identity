package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.FederationCertificate;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_federation_certificate.
 */
public interface FederationCertificateRepository {
    void save(FederationCertificate cert);
    Optional<FederationCertificate> findById(String id);
    List<FederationCertificate> findByConnectionId(String connectionId);
    List<FederationCertificate> findByConnectionIdAndStatus(String connectionId, String status);
    List<FederationCertificate> findByStatus(String status);
    List<FederationCertificate> findExpiringBefore(long threshold);
    void update(FederationCertificate cert);
    void updateStatus(String id, String status, long now, long version);
}
