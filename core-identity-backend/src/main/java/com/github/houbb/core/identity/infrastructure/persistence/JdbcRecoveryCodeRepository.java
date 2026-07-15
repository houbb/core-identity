package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.RecoveryCode;
import com.github.houbb.core.identity.application.port.RecoveryCodeRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class JdbcRecoveryCodeRepository implements RecoveryCodeRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcRecoveryCodeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(RecoveryCode code) {
        jdbcTemplate.update(
                "INSERT INTO identity_recovery_code (id, code_set_id, code_hash, status, used_at, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)",
                code.getId(), code.getCodeSetId(), code.getCodeHash(), code.getStatus(),
                code.getUsedAt(), code.getCreatedAt()
        );
    }

    @Override
    public void saveBatch(List<RecoveryCode> codes) {
        jdbcTemplate.batchUpdate(
                "INSERT INTO identity_recovery_code (id, code_set_id, code_hash, status, used_at, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)",
                codes,
                100,
                (PreparedStatement ps, RecoveryCode code) -> {
                    ps.setString(1, code.getId());
                    ps.setString(2, code.getCodeSetId());
                    ps.setString(3, code.getCodeHash());
                    ps.setString(4, code.getStatus());
                    if (code.getUsedAt() != null) {
                        ps.setLong(5, code.getUsedAt());
                    } else {
                        ps.setNull(5, java.sql.Types.BIGINT);
                    }
                    ps.setLong(6, code.getCreatedAt());
                }
        );
    }

    @Override
    public RecoveryCode findByCodeHash(String codeHash) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_recovery_code WHERE code_hash = ?",
                    new RecoveryCodeRowMapper(), codeHash);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void markUsed(String id, long usedAt) {
        jdbcTemplate.update(
                "UPDATE identity_recovery_code SET status = 'USED', used_at = ? WHERE id = ?",
                usedAt, id
        );
    }

    @Override
    public void markAllRevokedByCodeSetId(String codeSetId) {
        jdbcTemplate.update(
                "UPDATE identity_recovery_code SET status = 'REVOKED' WHERE code_set_id = ? AND status = 'ACTIVE'",
                codeSetId
        );
    }

    static class RecoveryCodeRowMapper implements RowMapper<RecoveryCode> {
        @Override
        public RecoveryCode mapRow(ResultSet rs, int rowNum) throws SQLException {
            RecoveryCode c = new RecoveryCode();
            c.setId(rs.getString("id"));
            c.setCodeSetId(rs.getString("code_set_id"));
            c.setCodeHash(rs.getString("code_hash"));
            c.setStatus(rs.getString("status"));
            c.setUsedAt(JdbcUserRepository.getNullableLong(rs, "used_at"));
            c.setCreatedAt(rs.getLong("created_at"));
            return c;
        }
    }
}
