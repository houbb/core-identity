package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.ServiceCredential;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_service_credential.
 */
public interface ServiceCredentialRepository {

    void save(ServiceCredential credential);

    Optional<ServiceCredential> findById(String id);

    List<ServiceCredential> findByServiceAccountId(String serviceAccountId);

    Optional<ServiceCredential> findActiveByServiceAccountId(String serviceAccountId);

    Optional<ServiceCredential> findByClientId(String clientId);

    void update(ServiceCredential credential);

    void updateLastUsedAt(String id, long now);

    void revokeByServiceAccountId(String serviceAccountId, long now);
}