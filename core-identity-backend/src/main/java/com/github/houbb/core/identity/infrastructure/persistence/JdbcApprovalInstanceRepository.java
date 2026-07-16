package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.ApprovalInstance;
import com.github.houbb.core.identity.application.port.ApprovalInstanceRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class JdbcApprovalInstanceRepository implements ApprovalInstanceRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcApprovalInstanceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(ApprovalInstance instance) {
        jdbcTemplate.update(
                "INSERT INTO identity_approval_instance (id, request_type, request_id, " +
                "status, current_step, created_at, updated_at, completed_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                instance.getId(), instance.getRequestType(), instance.getRequestId(),
                instance.getStatus(), instance.getCurrentStep(),
                instance.getCreatedAt(), instance.getUpdatedAt(), instance.getCompletedAt(), instance.getVersion()
        );
    }

    @Override
    public Optional<ApprovalInstance> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_approval_instance WHERE id = ?",
                    new ApprovalInstanceRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<ApprovalInstance> findByRequest(String requestType, String requestId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_approval_instance WHERE request_type = ? AND request_id = ?",
                    new ApprovalInstanceRowMapper(), requestType, requestId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void update(ApprovalInstance instance) {
        jdbcTemplate.update(
                "UPDATE identity_approval_instance SET status = ?, current_step = ?, " +
                "completed_at = ?, updated_at = ?, version = version + 1 WHERE id = ? AND version = ?",
                instance.getStatus(), instance.getCurrentStep(),
                instance.getCompletedAt(), instance.getUpdatedAt(), instance.getId(), instance.getVersion()
        );
    }

    @Override
    public void updateStatus(String id, String status, long completedAt, long now, long version) {
        jdbcTemplate.update(
                "UPDATE identity_approval_instance SET status = ?, completed_at = ?, " +
                "version = version + 1 WHERE id = ? AND version = ?",
                status, completedAt, id, version);
    }

    static class ApprovalInstanceRowMapper implements RowMapper<ApprovalInstance> {
        @Override
        public ApprovalInstance mapRow(ResultSet rs, int rowNum) throws SQLException {
            ApprovalInstance inst = new ApprovalInstance();
            inst.setId(rs.getString("id"));
            inst.setRequestType(rs.getString("request_type"));
            inst.setRequestId(rs.getString("request_id"));
            inst.setStatus(rs.getString("status"));
            inst.setCurrentStep(rs.getInt("current_step"));
            inst.setCreatedAt(rs.getLong("created_at"));
            inst.setUpdatedAt(rs.getLong("updated_at"));
            inst.setCompletedAt(getLongOrNull(rs, "completed_at"));
            inst.setVersion(rs.getLong("version"));
            return inst;
        }

        private Long getLongOrNull(ResultSet rs, String column) throws SQLException {
            long val = rs.getLong(column);
            return rs.wasNull() ? null : val;
        }
    }
}