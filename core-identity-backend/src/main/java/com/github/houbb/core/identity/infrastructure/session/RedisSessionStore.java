package com.github.houbb.core.identity.infrastructure.session;

import com.github.houbb.core.identity.application.domain.Session;
import com.github.houbb.core.identity.application.port.SessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Redis-backed session store (P7.1, stub implementation).
 * <p>
 * Currently delegates to the database-based SessionStore.
 * Full Redis integration will be implemented in P7.2 (Cache layer).
 * <p>
 * The design intent: Redis is a hot cache, not the source of truth.
 * Session durability always comes from the database.
 */
public class RedisSessionStore implements SessionStore {

    private static final Logger log = LoggerFactory.getLogger(RedisSessionStore.class);

    private final SessionStore delegate;

    public RedisSessionStore(SessionStore delegate) {
        this.delegate = delegate;
        log.info("RedisSessionStore initialized — using database delegate (Redis cache not yet implemented)");
    }

    @Override
    public void save(Session session) {
        delegate.save(session);
    }

    @Override
    public Optional<Session> findByTokenHash(String tokenHash) {
        return delegate.findByTokenHash(tokenHash);
    }

    @Override
    public Optional<Session> findById(String id) {
        return delegate.findById(id);
    }

    @Override
    public List<Session> findByUserIdAndStatus(String userId, String status) {
        return delegate.findByUserIdAndStatus(userId, status);
    }

    @Override
    public List<Session> findActiveByUserId(String userId, long now) {
        return delegate.findActiveByUserId(userId, now);
    }

    @Override
    public void update(Session session) {
        delegate.update(session);
    }

    @Override
    public void updateLastOrganizationId(String id, String lastOrganizationId, long permissionVersion,
                                         long now, long version) {
        delegate.updateLastOrganizationId(id, lastOrganizationId, permissionVersion, now, version);
    }

    @Override
    public void revokeByUserId(String userId, String reason, long revokedAt) {
        delegate.revokeByUserId(userId, reason, revokedAt);
    }

    @Override
    public void revokeExceptCurrent(String userId, String currentSessionId, String reason, long revokedAt) {
        delegate.revokeExceptCurrent(userId, currentSessionId, reason, revokedAt);
    }

    @Override
    public void expireIdle(long beforeTimestamp) {
        delegate.expireIdle(beforeTimestamp);
    }
}
