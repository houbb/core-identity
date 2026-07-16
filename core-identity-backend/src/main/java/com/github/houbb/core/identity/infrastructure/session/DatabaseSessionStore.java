package com.github.houbb.core.identity.infrastructure.session;

import com.github.houbb.core.identity.application.domain.Session;
import com.github.houbb.core.identity.application.port.SessionRepository;
import com.github.houbb.core.identity.application.port.SessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Database-backed session store (P7.1).
 * <p>
 * Delegates directly to {@link SessionRepository}.
 * This is the default implementation for standalone and standard modes.
 * No Redis dependency.
 */
public class DatabaseSessionStore implements SessionStore {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSessionStore.class);

    private final SessionRepository sessionRepository;

    public DatabaseSessionStore(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
        log.info("DatabaseSessionStore initialized — sessions stored in database");
    }

    @Override
    public void save(Session session) {
        sessionRepository.save(session);
    }

    @Override
    public Optional<Session> findByTokenHash(String tokenHash) {
        return sessionRepository.findByTokenHash(tokenHash);
    }

    @Override
    public Optional<Session> findById(String id) {
        return sessionRepository.findById(id);
    }

    @Override
    public List<Session> findByUserIdAndStatus(String userId, String status) {
        return sessionRepository.findByUserIdAndStatus(userId, status);
    }

    @Override
    public List<Session> findActiveByUserId(String userId, long now) {
        return sessionRepository.findActiveByUserId(userId, now);
    }

    @Override
    public void update(Session session) {
        sessionRepository.update(session);
    }

    @Override
    public void updateLastOrganizationId(String id, String lastOrganizationId, long permissionVersion,
                                         long now, long version) {
        sessionRepository.updateLastOrganizationId(id, lastOrganizationId, permissionVersion, now, version);
    }

    @Override
    public void revokeByUserId(String userId, String reason, long revokedAt) {
        sessionRepository.revokeByUserId(userId, reason, revokedAt);
    }

    @Override
    public void revokeExceptCurrent(String userId, String currentSessionId, String reason, long revokedAt) {
        sessionRepository.revokeExceptCurrent(userId, currentSessionId, reason, revokedAt);
    }

    @Override
    public void expireIdle(long beforeTimestamp) {
        sessionRepository.expireIdle(beforeTimestamp);
    }
}
