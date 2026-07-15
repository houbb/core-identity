package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.Session;
import com.github.houbb.core.identity.application.port.SessionRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcSessionRepository implements SessionRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSessionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(Session session) {
        jdbcTemplate.update(
                "INSERT INTO identity_session (id, user_id, session_type, token_hash, status, ip_address, " +
                "user_agent, device_name, last_active_at, idle_expires_at, absolute_expires_at, " +
                "revoked_at, revoke_reason, created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                session.getId(), session.getUserId(), session.getSessionType(), session.getTokenHash(),
                session.getStatus(), session.getIpAddress(), session.getUserAgent(), session.getDeviceName(),
                session.getLastActiveAt(), session.getIdleExpiresAt(), session.getAbsoluteExpiresAt(),
                session.getRevokedAt(), session.getRevokeReason(), session.getCreatedAt(),
                session.getUpdatedAt(), session.getVersion()
        );
    }

    @Override
    public Optional<Session> findByTokenHash(String tokenHash) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_session WHERE token_hash = ?", new SessionRowMapper(), tokenHash));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Session> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_session WHERE id = ?", new SessionRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Session> findByUserIdAndStatus(String userId, String status) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_session WHERE user_id = ? AND status = ? ORDER BY created_at DESC",
                new SessionRowMapper(), userId, status);
    }

    @Override
    public List<Session> findActiveByUserId(String userId, long now) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_session WHERE user_id = ? AND status = 'ACTIVE' " +
                "AND absolute_expires_at > ? AND idle_expires_at > ? ORDER BY created_at DESC",
                new SessionRowMapper(), userId, now, now);
    }

    @Override
    public void update(Session session) {
        jdbcTemplate.update(
                "UPDATE identity_session SET last_active_at = ?, idle_expires_at = ?, status = ?, " +
                "revoked_at = ?, revoke_reason = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                session.getLastActiveAt(), session.getIdleExpiresAt(), session.getStatus(),
                session.getRevokedAt(), session.getRevokeReason(), session.getUpdatedAt(),
                session.getId(), session.getVersion()
        );
    }

    @Override
    public void revokeByUserId(String userId, String reason, long revokedAt) {
        jdbcTemplate.update(
                "UPDATE identity_session SET status = 'REVOKED', revoke_reason = ?, revoked_at = ?, " +
                "updated_at = ? WHERE user_id = ? AND status = 'ACTIVE'",
                reason, revokedAt, System.currentTimeMillis(), userId
        );
    }

    @Override
    public void revokeExceptCurrent(String userId, String currentSessionId, String reason, long revokedAt) {
        jdbcTemplate.update(
                "UPDATE identity_session SET status = 'REVOKED', revoke_reason = ?, revoked_at = ?, " +
                "updated_at = ? WHERE user_id = ? AND status = 'ACTIVE' AND id != ?",
                reason, revokedAt, System.currentTimeMillis(), userId, currentSessionId
        );
    }

    @Override
    public void expireIdle(long beforeTimestamp) {
        jdbcTemplate.update(
                "UPDATE identity_session SET status = 'EXPIRED', updated_at = ? " +
                "WHERE status = 'ACTIVE' AND idle_expires_at < ?",
                System.currentTimeMillis(), beforeTimestamp
        );
    }

    static class SessionRowMapper implements RowMapper<Session> {
        @Override
        public Session mapRow(ResultSet rs, int rowNum) throws SQLException {
            Session s = new Session();
            s.setId(rs.getString("id"));
            s.setUserId(rs.getString("user_id"));
            s.setSessionType(rs.getString("session_type"));
            s.setTokenHash(rs.getString("token_hash"));
            s.setStatus(rs.getString("status"));
            s.setIpAddress(rs.getString("ip_address"));
            s.setUserAgent(rs.getString("user_agent"));
            s.setDeviceName(rs.getString("device_name"));
            s.setLastActiveAt(rs.getLong("last_active_at"));
            s.setIdleExpiresAt(rs.getLong("idle_expires_at"));
            s.setAbsoluteExpiresAt(rs.getLong("absolute_expires_at"));
            s.setRevokedAt(JdbcUserRepository.getNullableLong(rs, "revoked_at"));
            s.setRevokeReason(rs.getString("revoke_reason"));
            s.setCreatedAt(rs.getLong("created_at"));
            s.setUpdatedAt(rs.getLong("updated_at"));
            s.setVersion(rs.getLong("version"));
            return s;
        }
    }
}