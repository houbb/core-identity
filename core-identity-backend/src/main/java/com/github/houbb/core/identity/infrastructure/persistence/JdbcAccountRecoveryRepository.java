package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.AccountRecovery;
import com.github.houbb.core.identity.application.port.AccountRecoveryRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcAccountRecoveryRepository implements AccountRecoveryRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAccountRecoveryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(AccountRecovery r) {
        jdbcTemplate.update(
                "INSERT INTO identity_account_recovery (id, user_id, recovery_type, status, risk_level, " +
                "required_evidence_level, initiated_ip, initiated_device_id, cooling_off_until, " +
                "approved_by, rejected_by, completed_at, cancelled_at, created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                r.getId(), r.getUserId(), r.getRecoveryType(), r.getStatus(), r.getRiskLevel(),
                r.getRequiredEvidenceLevel(), r.getInitiatedIp(), r.getInitiatedDeviceId(),
                r.getCoolingOffUntil(), r.getApprovedBy(), r.getRejectedBy(),
                r.getCompletedAt(), r.getCancelledAt(), r.getCreatedAt(), r.getUpdatedAt(), r.getVersion()
        );
    }

    @Override
    public void update(AccountRecovery r) {
        jdbcTemplate.update(
                "UPDATE identity_account_recovery SET status = ?, cooling_off_until = ?, " +
                "completed_at = ?, cancelled_at = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                r.getStatus(), r.getCoolingOffUntil(), r.getCompletedAt(), r.getCancelledAt(),
                r.getUpdatedAt(), r.getId(), r.getVersion()
        );
    }

    @Override
    public Optional<AccountRecovery> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_account_recovery WHERE id = ?",
                    new RecoveryRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<AccountRecovery> findPending() {
        return jdbcTemplate.query(
                "SELECT * FROM identity_account_recovery WHERE status IN ('PENDING_VERIFICATION','COOLING_OFF','PENDING_REVIEW') ORDER BY created_at DESC",
                new RecoveryRowMapper());
    }

    static class RecoveryRowMapper implements RowMapper<AccountRecovery> {
        @Override
        public AccountRecovery mapRow(ResultSet rs, int rowNum) throws SQLException {
            AccountRecovery r = new AccountRecovery();
            r.setId(rs.getString("id"));
            r.setUserId(rs.getString("user_id"));
            r.setRecoveryType(rs.getString("recovery_type"));
            r.setStatus(rs.getString("status"));
            r.setRiskLevel(rs.getString("risk_level"));
            r.setRequiredEvidenceLevel(rs.getString("required_evidence_level"));
            r.setInitiatedIp(rs.getString("initiated_ip"));
            r.setInitiatedDeviceId(rs.getString("initiated_device_id"));
            r.setCoolingOffUntil(getNullableLong(rs, "cooling_off_until"));
            r.setApprovedBy(rs.getString("approved_by"));
            r.setRejectedBy(rs.getString("rejected_by"));
            r.setCompletedAt(getNullableLong(rs, "completed_at"));
            r.setCancelledAt(getNullableLong(rs, "cancelled_at"));
            r.setCreatedAt(rs.getLong("created_at"));
            r.setUpdatedAt(rs.getLong("updated_at"));
            r.setVersion(rs.getLong("version"));
            return r;
        }

        private Long getNullableLong(ResultSet rs, String column) throws SQLException {
            long val = rs.getLong(column);
            return rs.wasNull() ? null : val;
        }
    }
}
