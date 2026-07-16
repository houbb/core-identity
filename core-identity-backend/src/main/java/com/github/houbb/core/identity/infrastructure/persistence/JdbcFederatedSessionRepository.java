package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.FederatedSession;
import com.github.houbb.core.identity.application.port.FederatedSessionRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class JdbcFederatedSessionRepository implements FederatedSessionRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcFederatedSessionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(FederatedSession session) {
        jdbcTemplate.update(
                "INSERT INTO identity_federated_session (id, session_id, connection_id, external_identity_id, " +
                "upstream_session_id, upstream_subject, upstream_auth_time, upstream_acr, upstream_amr_json, " +
                "logout_status, created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                session.getId(), session.getSessionId(), session.getConnectionId(), session.getExternalIdentityId(),
                session.getUpstreamSessionId(), session.getUpstreamSubject(), session.getUpstreamAuthTime(),
                session.getUpstreamAcr(), session.getUpstreamAmrJson(),
                session.getLogoutStatus(), session.getCreatedAt(), session.getUpdatedAt(), session.getVersion()
        );
    }

    @Override
    public Optional<FederatedSession> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_federated_session WHERE id = ?", new FederatedSessionRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<FederatedSession> findBySessionId(String sessionId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_federated_session WHERE session_id = ?", new FederatedSessionRowMapper(), sessionId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<FederatedSession> findByExternalIdentityId(String externalIdentityId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_federated_session WHERE external_identity_id = ?", new FederatedSessionRowMapper(), externalIdentityId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void update(FederatedSession session) {
        jdbcTemplate.update(
                "UPDATE identity_federated_session SET session_id = ?, connection_id = ?, " +
                "external_identity_id = ?, upstream_session_id = ?, upstream_subject = ?, " +
                "upstream_auth_time = ?, upstream_acr = ?, upstream_amr_json = ?, " +
                "logout_status = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                session.getSessionId(), session.getConnectionId(),
                session.getExternalIdentityId(), session.getUpstreamSessionId(), session.getUpstreamSubject(),
                session.getUpstreamAuthTime(), session.getUpstreamAcr(), session.getUpstreamAmrJson(),
                session.getLogoutStatus(), session.getUpdatedAt(),
                session.getId(), session.getVersion()
        );
    }

    @Override
    public void updateLogoutStatus(String id, String logoutStatus, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_federated_session SET logout_status = ?, updated_at = ?, " +
                "version = version + 1 WHERE id = ? AND version = ?",
                logoutStatus, now, id, version);
    }

    static class FederatedSessionRowMapper implements RowMapper<FederatedSession> {
        @Override
        public FederatedSession mapRow(ResultSet rs, int rowNum) throws SQLException {
            FederatedSession s = new FederatedSession();
            s.setId(rs.getString("id"));
            s.setSessionId(rs.getString("session_id"));
            s.setConnectionId(rs.getString("connection_id"));
            s.setExternalIdentityId(rs.getString("external_identity_id"));
            s.setUpstreamSessionId(getStringOrNull(rs, "upstream_session_id"));
            s.setUpstreamSubject(getStringOrNull(rs, "upstream_subject"));
            s.setUpstreamAuthTime(getLongOrNull(rs, "upstream_auth_time"));
            s.setUpstreamAcr(getStringOrNull(rs, "upstream_acr"));
            s.setUpstreamAmrJson(getStringOrNull(rs, "upstream_amr_json"));
            s.setLogoutStatus(getStringOrNull(rs, "logout_status"));
            s.setCreatedAt(rs.getLong("created_at"));
            s.setUpdatedAt(rs.getLong("updated_at"));
            s.setVersion(rs.getLong("version"));
            return s;
        }
    }

    private static String getStringOrNull(ResultSet rs, String column) throws SQLException {
        try { return rs.getString(column); } catch (SQLException e) { return null; }
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        try { long val = rs.getLong(column); return rs.wasNull() ? null : val; } catch (SQLException e) { return null; }
    }
}