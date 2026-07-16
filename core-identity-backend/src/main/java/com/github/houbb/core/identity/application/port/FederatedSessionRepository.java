package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.FederatedSession;

import java.util.Optional;

/**
 * Repository for identity_federated_session.
 */
public interface FederatedSessionRepository {
    void save(FederatedSession session);
    Optional<FederatedSession> findById(String id);
    Optional<FederatedSession> findBySessionId(String sessionId);
    Optional<FederatedSession> findByExternalIdentityId(String externalIdentityId);
    void update(FederatedSession session);
    void updateLogoutStatus(String id, String logoutStatus, long now, long version);
}
