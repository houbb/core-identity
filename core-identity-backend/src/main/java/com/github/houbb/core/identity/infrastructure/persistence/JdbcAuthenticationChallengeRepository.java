package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.AuthenticationChallenge;
import com.github.houbb.core.identity.application.port.AuthenticationChallengeRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class JdbcAuthenticationChallengeRepository implements AuthenticationChallengeRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAuthenticationChallengeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(AuthenticationChallenge c) {
        jdbcTemplate.update(
                "INSERT INTO identity_authentication_challenge (id, user_id, session_id, challenge_type, " +
                "required_level, allowed_methods_json, challenge_hash, context_json, status, attempt_count, " +
                "expires_at, completed_at, created_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                c.getId(), c.getUserId(), c.getSessionId(), c.getChallengeType(),
                c.getRequiredLevel(), c.getAllowedMethodsJson(), c.getChallengeHash(), c.getContextJson(),
                c.getStatus(), c.getAttemptCount(), c.getExpiresAt(), c.getCompletedAt(),
                c.getCreatedAt(), c.getVersion()
        );
    }

    @Override
    public Optional<AuthenticationChallenge> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_authentication_challenge WHERE id = ?",
                    new ChallengeRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void updateStatus(String id, String status, Long completedAt, int attemptCount, long version) {
        jdbcTemplate.update(
                "UPDATE identity_authentication_challenge SET status = ?, completed_at = ?, " +
                "attempt_count = ?, version = version + 1 WHERE id = ? AND version = ?",
                status, completedAt, attemptCount, id, version
        );
    }

    @Override
    public void expireStale(long beforeTimestamp) {
        jdbcTemplate.update(
                "UPDATE identity_authentication_challenge SET status = 'EXPIRED' " +
                "WHERE status = 'PENDING' AND expires_at < ?",
                beforeTimestamp
        );
    }

    static class ChallengeRowMapper implements RowMapper<AuthenticationChallenge> {
        @Override
        public AuthenticationChallenge mapRow(ResultSet rs, int rowNum) throws SQLException {
            AuthenticationChallenge c = new AuthenticationChallenge();
            c.setId(rs.getString("id"));
            c.setUserId(rs.getString("user_id"));
            c.setSessionId(rs.getString("session_id"));
            c.setChallengeType(rs.getString("challenge_type"));
            c.setRequiredLevel(rs.getString("required_level"));
            c.setAllowedMethodsJson(rs.getString("allowed_methods_json"));
            c.setChallengeHash(rs.getString("challenge_hash"));
            c.setContextJson(rs.getString("context_json"));
            c.setStatus(rs.getString("status"));
            c.setAttemptCount(rs.getInt("attempt_count"));
            c.setExpiresAt(rs.getLong("expires_at"));
            c.setCompletedAt(JdbcUserRepository.getNullableLong(rs, "completed_at"));
            c.setCreatedAt(rs.getLong("created_at"));
            c.setVersion(rs.getLong("version"));
            return c;
        }
    }
}
