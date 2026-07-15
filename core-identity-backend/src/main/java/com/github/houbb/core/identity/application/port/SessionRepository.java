package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.Session;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_session.
 */
public interface SessionRepository {

    void save(Session session);

    Optional<Session> findByTokenHash(String tokenHash);

    Optional<Session> findById(String id);

    List<Session> findByUserIdAndStatus(String userId, String status);

    List<Session> findActiveByUserId(String userId, long now);

    void update(Session session);

    void updateLastOrganizationId(String id, String lastOrganizationId, long permissionVersion, long now, long version);

    void revokeByUserId(String userId, String reason, long revokedAt);

    void revokeExceptCurrent(String userId, String currentSessionId, String reason, long revokedAt);

    void expireIdle(long beforeTimestamp);
}