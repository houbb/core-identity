package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.StepUpGrant;
import com.github.houbb.core.identity.application.port.StepUpGrantRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class JdbcStepUpGrantRepository implements StepUpGrantRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcStepUpGrantRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(StepUpGrant g) {
        jdbcTemplate.update(
                "INSERT INTO identity_step_up_grant (id, user_id, session_id, authentication_level, " +
                "allowed_actions_json, status, issued_at, expires_at, consumed_at, created_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                g.getId(), g.getUserId(), g.getSessionId(), g.getAuthenticationLevel(),
                g.getAllowedActionsJson(), g.getStatus(), g.getIssuedAt(), g.getExpiresAt(),
                g.getConsumedAt(), g.getCreatedAt(), g.getVersion()
        );
    }

    @Override
    public Optional<StepUpGrant> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_step_up_grant WHERE id = ?", new StepUpGrantRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<StepUpGrant> findActiveByUserIdAndSession(String userId, String sessionId, long now) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_step_up_grant WHERE user_id = ? AND session_id = ? " +
                    "AND status = 'ACTIVE' AND expires_at > ? ORDER BY issued_at DESC LIMIT 1",
                    new StepUpGrantRowMapper(), userId, sessionId, now));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void consume(String id, long consumedAt, long version) {
        jdbcTemplate.update(
                "UPDATE identity_step_up_grant SET status = 'CONSUMED', consumed_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                consumedAt, id, version
        );
    }

    @Override
    public void expireStale(long beforeTimestamp) {
        jdbcTemplate.update(
                "UPDATE identity_step_up_grant SET status = 'EXPIRED' " +
                "WHERE status = 'ACTIVE' AND expires_at < ?",
                beforeTimestamp
        );
    }

    static class StepUpGrantRowMapper implements RowMapper<StepUpGrant> {
        @Override
        public StepUpGrant mapRow(ResultSet rs, int rowNum) throws SQLException {
            StepUpGrant g = new StepUpGrant();
            g.setId(rs.getString("id"));
            g.setUserId(rs.getString("user_id"));
            g.setSessionId(rs.getString("session_id"));
            g.setAuthenticationLevel(rs.getString("authentication_level"));
            g.setAllowedActionsJson(rs.getString("allowed_actions_json"));
            g.setStatus(rs.getString("status"));
            g.setIssuedAt(rs.getLong("issued_at"));
            g.setExpiresAt(rs.getLong("expires_at"));
            g.setConsumedAt(JdbcUserRepository.getNullableLong(rs, "consumed_at"));
            g.setCreatedAt(rs.getLong("created_at"));
            g.setVersion(rs.getLong("version"));
            return g;
        }
    }
}
