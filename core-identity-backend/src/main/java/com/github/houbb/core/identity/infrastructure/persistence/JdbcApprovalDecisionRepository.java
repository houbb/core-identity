package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.ApprovalDecision;
import com.github.houbb.core.identity.application.port.ApprovalDecisionRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcApprovalDecisionRepository implements ApprovalDecisionRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcApprovalDecisionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(ApprovalDecision decision) {
        jdbcTemplate.update(
                "INSERT INTO identity_approval_decision (id, approval_step_id, approver_user_id, " +
                "decision, reason, decided_at, authentication_level, request_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                decision.getId(), decision.getApprovalStepId(), decision.getApproverUserId(),
                decision.getDecision(), decision.getReason(), decision.getDecidedAt(),
                decision.getAuthenticationLevel(), decision.getRequestId()
        );
    }

    @Override
    public Optional<ApprovalDecision> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_approval_decision WHERE id = ?",
                    new ApprovalDecisionRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<ApprovalDecision> findByStepId(String approvalStepId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_approval_decision WHERE approval_step_id = ? ORDER BY decided_at",
                new ApprovalDecisionRowMapper(), approvalStepId);
    }

    @Override
    public List<ApprovalDecision> findByApproverId(String approverUserId) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_approval_decision WHERE approver_user_id = ? ORDER BY decided_at DESC",
                new ApprovalDecisionRowMapper(), approverUserId);
    }

    @Override
    public int countByStepIdAndDecision(String approvalStepId, String decision) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM identity_approval_decision WHERE approval_step_id = ? AND decision = ?",
                Integer.class, approvalStepId, decision);
        return count != null ? count : 0;
    }

    static class ApprovalDecisionRowMapper implements RowMapper<ApprovalDecision> {
        @Override
        public ApprovalDecision mapRow(ResultSet rs, int rowNum) throws SQLException {
            ApprovalDecision dec = new ApprovalDecision();
            dec.setId(rs.getString("id"));
            dec.setApprovalStepId(rs.getString("approval_step_id"));
            dec.setApproverUserId(rs.getString("approver_user_id"));
            dec.setDecision(rs.getString("decision"));
            dec.setReason(rs.getString("reason"));
            dec.setDecidedAt(rs.getLong("decided_at"));
            dec.setAuthenticationLevel(rs.getString("authentication_level"));
            dec.setRequestId(rs.getString("request_id"));
            return dec;
        }
    }
}