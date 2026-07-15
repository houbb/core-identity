package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.RecoveryCodeSet;
import com.github.houbb.core.identity.application.port.RecoveryCodeSetRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class JdbcRecoveryCodeSetRepository implements RecoveryCodeSetRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcRecoveryCodeSetRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(RecoveryCodeSet set) {
        jdbcTemplate.update(
                "INSERT INTO identity_recovery_code_set (id, user_id, status, total_count, remaining_count, " +
                "generated_at, revoked_at, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                set.getId(), set.getUserId(), set.getStatus(), set.getTotalCount(), set.getRemainingCount(),
                set.getGeneratedAt(), set.getRevokedAt(), set.getVersion()
        );
    }

    @Override
    public Optional<RecoveryCodeSet> findByUserIdAndStatus(String userId, String status) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_recovery_code_set WHERE user_id = ? AND status = ?",
                    new RecoveryCodeSetRowMapper(), userId, status));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void updateStatus(String id, String status, long version) {
        jdbcTemplate.update(
                "UPDATE identity_recovery_code_set SET status = ?, version = version + 1 WHERE id = ? AND version = ?",
                status, id, version
        );
    }

    @Override
    public void decrementRemaining(String id, long version) {
        jdbcTemplate.update(
                "UPDATE identity_recovery_code_set SET remaining_count = remaining_count - 1, " +
                "version = version + 1 WHERE id = ? AND version = ?",
                id, version
        );
    }

    @Override
    public void revokeByUserId(String userId, long revokedAt) {
        jdbcTemplate.update(
                "UPDATE identity_recovery_code_set SET status = 'REVOKED', revoked_at = ? WHERE user_id = ? AND status = 'ACTIVE'",
                revokedAt, userId
        );
    }

    static class RecoveryCodeSetRowMapper implements RowMapper<RecoveryCodeSet> {
        @Override
        public RecoveryCodeSet mapRow(ResultSet rs, int rowNum) throws SQLException {
            RecoveryCodeSet s = new RecoveryCodeSet();
            s.setId(rs.getString("id"));
            s.setUserId(rs.getString("user_id"));
            s.setStatus(rs.getString("status"));
            s.setTotalCount(rs.getInt("total_count"));
            s.setRemainingCount(rs.getInt("remaining_count"));
            s.setGeneratedAt(rs.getLong("generated_at"));
            s.setRevokedAt(JdbcUserRepository.getNullableLong(rs, "revoked_at"));
            s.setVersion(rs.getLong("version"));
            return s;
        }
    }
}
