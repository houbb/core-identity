package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.PrivilegedActivation;
import com.github.houbb.core.identity.application.port.PrivilegedActivationRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcPrivilegedActivationRepository implements PrivilegedActivationRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPrivilegedActivationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(PrivilegedActivation activation) {
        jdbcTemplate.update(
                "INSERT INTO identity_privileged_activation (id, grant_id, user_id, organization_id, " +
                "role_id, reason, ticket_reference, status, authentication_level, session_id, " +
                "activated_at, expires_at, ended_at, created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                activation.getId(), activation.getGrantId(), activation.getUserId(),
                activation.getOrganizationId(), activation.getRoleId(), activation.getReason(),
                activation.getTicketReference(), activation.getStatus(), activation.getAuthenticationLevel(),
                activation.getSessionId(), activation.getActivatedAt(), activation.getExpiresAt(),
                activation.getEndedAt(), activation.getCreatedAt(), activation.getUpdatedAt(),
                activation.getVersion()
        );
    }

    @Override
    public Optional<PrivilegedActivation> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_privileged_activation WHERE id = ?",
                    new PrivilegedActivationRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<PrivilegedActivation> findByUserId(String userId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_privileged_activation WHERE user_id = ? ORDER BY activated_at DESC",
                new PrivilegedActivationRowMapper(), userId);
    }

    @Override
    public List<PrivilegedActivation> findActiveByUserId(String userId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_privileged_activation WHERE user_id = ? AND status = 'ACTIVE'",
                new PrivilegedActivationRowMapper(), userId);
    }

    @Override
    public Optional<PrivilegedActivation> findActiveByUserIdAndRole(String userId, String roleId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_privileged_activation WHERE user_id = ? AND role_id = ? AND status = 'ACTIVE'",
                    new PrivilegedActivationRowMapper(), userId, roleId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<PrivilegedActivation> findExpiringActivations(long beforeTimestamp) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_privileged_activation WHERE expires_at <= ? AND status = 'ACTIVE'",
                new PrivilegedActivationRowMapper(), beforeTimestamp);
    }

    @Override
    public void update(PrivilegedActivation activation) {
        jdbcTemplate.update(
                "UPDATE identity_privileged_activation SET status = ?, reason = ?, " +
                "updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                activation.getStatus(), activation.getReason(),
                activation.getUpdatedAt(), activation.getId(), activation.getVersion()
        );
    }

    @Override
    public void end(String id, long endedAt, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_privileged_activation SET status = 'ENDED', ended_at = ?, " +
                "updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                endedAt, now, id, version);
    }

    static class PrivilegedActivationRowMapper implements RowMapper<PrivilegedActivation> {
        @Override
        public PrivilegedActivation mapRow(ResultSet rs, int rowNum) throws SQLException {
            PrivilegedActivation act = new PrivilegedActivation();
            act.setId(rs.getString("id"));
            act.setGrantId(rs.getString("grant_id"));
            act.setUserId(rs.getString("user_id"));
            act.setOrganizationId(rs.getString("organization_id"));
            act.setRoleId(rs.getString("role_id"));
            act.setReason(rs.getString("reason"));
            act.setTicketReference(rs.getString("ticket_reference"));
            act.setStatus(rs.getString("status"));
            act.setAuthenticationLevel(rs.getString("authentication_level"));
            act.setSessionId(rs.getString("session_id"));
            act.setActivatedAt(rs.getLong("activated_at"));
            act.setExpiresAt(rs.getLong("expires_at"));
            act.setEndedAt(getLongOrNull(rs, "ended_at"));
            act.setCreatedAt(rs.getLong("created_at"));
            act.setUpdatedAt(rs.getLong("updated_at"));
            act.setVersion(rs.getLong("version"));
            return act;
        }

        private Long getLongOrNull(ResultSet rs, String column) throws SQLException {
            long val = rs.getLong(column);
            return rs.wasNull() ? null : val;
        }
    }
}