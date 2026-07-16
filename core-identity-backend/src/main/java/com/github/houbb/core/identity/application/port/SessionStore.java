package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.Session;

import java.util.List;
import java.util.Optional;

/**
 * Session store abstraction (P7.1).
 * <p>
 * In standalone mode, this delegates directly to the database.
 * In enterprise mode, Redis is used as a hot cache with DB as fallback.
 * <p>
 * The key difference from {@link SessionRepository}: this is NOT just a DB
 * repository — it represents the "distributed session state" contract that
 * hides whether sessions live in DB, Redis, or both.
 */
public interface SessionStore {

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
