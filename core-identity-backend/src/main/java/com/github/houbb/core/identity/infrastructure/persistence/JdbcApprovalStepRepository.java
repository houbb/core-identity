package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.ApprovalStep;
import com.github.houbb.core.identity.application.port.ApprovalStepRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcApprovalStepRepository implements ApprovalStepRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcApprovalStepRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(ApprovalStep step) {
        jdbcTemplate.update(
                "INSERT INTO identity_approval_step (id, approval_instance_id, step_order, " +
                "approval_mode, required_approvals, approver_type, approver_reference, " +
                "status, due_at, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                step.getId(), step.getApprovalInstanceId(), step.getStepOrder(),
                step.getApprovalMode(), step.getRequiredApprovals(), step.getApproverType(),
                step.getApproverReference(), step.getStatus(), step.getDueAt(), step.getCreatedAt()
        );
    }

    @Override
    public void saveBatch(List<ApprovalStep> steps) {
        for (ApprovalStep step : steps) {
            save(step);
        }
    }

    @Override
    public Optional<ApprovalStep> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_approval_step WHERE id = ?",
                    new ApprovalStepRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<ApprovalStep> findByApprovalInstanceId(String approvalInstanceId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_approval_step WHERE approval_instance_id = ? ORDER BY step_order",
                new ApprovalStepRowMapper(), approvalInstanceId);
    }

    @Override
    public List<ApprovalStep> findByApprovalInstanceIdAndStatus(String approvalInstanceId, String status) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_approval_step WHERE approval_instance_id = ? AND status = ? ORDER BY step_order",
                new ApprovalStepRowMapper(), approvalInstanceId, status);
    }

    @Override
    public void update(ApprovalStep step) {
        jdbcTemplate.update(
                "UPDATE identity_approval_step SET approval_mode = ?, required_approvals = ?, " +
                "approver_type = ?, approver_reference = ?, status = ?, due_at = ? " +
                "WHERE id = ?",
                step.getApprovalMode(), step.getRequiredApprovals(),
                step.getApproverType(), step.getApproverReference(),
                step.getStatus(), step.getDueAt(), step.getId()
        );
    }

    @Override
    public void updateStatus(String id, String status, long now) {
        jdbcTemplate.update(
                "UPDATE identity_approval_step SET status = ? WHERE id = ?",
                status, id);
    }

    @Override
    public void deleteByApprovalInstanceId(String approvalInstanceId) {
        jdbcTemplate.update(
                "DELETE FROM identity_approval_step WHERE approval_instance_id = ?",
                approvalInstanceId);
    }

    static class ApprovalStepRowMapper implements RowMapper<ApprovalStep> {
        @Override
        public ApprovalStep mapRow(ResultSet rs, int rowNum) throws SQLException {
            ApprovalStep step = new ApprovalStep();
            step.setId(rs.getString("id"));
            step.setApprovalInstanceId(rs.getString("approval_instance_id"));
            step.setStepOrder(rs.getInt("step_order"));
            step.setApprovalMode(rs.getString("approval_mode"));
            step.setRequiredApprovals(rs.getInt("required_approvals"));
            step.setApproverType(rs.getString("approver_type"));
            step.setApproverReference(rs.getString("approver_reference"));
            step.setStatus(rs.getString("status"));
            step.setDueAt(getLongOrNull(rs, "due_at"));
            step.setCreatedAt(rs.getLong("created_at"));
            return step;
        }

        private Long getLongOrNull(ResultSet rs, String column) throws SQLException {
            long val = rs.getLong(column);
            return rs.wasNull() ? null : val;
        }
    }
}