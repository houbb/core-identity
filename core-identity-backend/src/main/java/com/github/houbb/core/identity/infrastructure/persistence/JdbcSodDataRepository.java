package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.port.SodDataRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class JdbcSodDataRepository implements SodDataRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSodDataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ── Policy Items ─────────────────────────────────────────────

    @Override
    public void insertPolicyItem(String id, String policyId, String leftEntitlementId,
                                 String rightEntitlementId, String riskLevel) {
        jdbcTemplate.update(
                "INSERT INTO identity_sod_policy_item (id, policy_id, left_entitlement_id, " +
                "right_entitlement_id, risk_level) VALUES (?, ?, ?, ?, ?)",
                id, policyId, leftEntitlementId, rightEntitlementId, riskLevel
        );
    }

    @Override
    public List<Map<String, Object>> findPolicyItemsByStatus(String policyStatus) {
        return jdbcTemplate.queryForList(
                "SELECT pi.* FROM identity_sod_policy_item pi " +
                "JOIN identity_sod_policy p ON pi.policy_id = p.id " +
                "WHERE p.status = ?",
                policyStatus);
    }

    // ── Conflicts ────────────────────────────────────────────────

    @Override
    public void insertConflict(String id, String policyId, String subjectId,
                               String leftGrantId, String rightGrantId, long detectedAt) {
        jdbcTemplate.update(
                "INSERT INTO identity_sod_conflict (id, policy_id, subject_id, left_grant_id, " +
                "right_grant_id, detected_at, status) VALUES (?, ?, ?, ?, ?, ?, 'OPEN')",
                id, policyId, subjectId, leftGrantId, rightGrantId, detectedAt
        );
    }

    @Override
    public List<Map<String, Object>> findOpenConflictsByPolicyAndSubject(String policyId, String subjectId) {
        return jdbcTemplate.queryForList(
                "SELECT * FROM identity_sod_conflict " +
                "WHERE policy_id = ? AND subject_id = ? AND status = 'OPEN'",
                policyId, subjectId);
    }

    @Override
    public List<Map<String, Object>> findConflictsByOrg(String organizationId) {
        return jdbcTemplate.queryForList(
                "SELECT sc.* FROM identity_sod_conflict sc " +
                "JOIN identity_sod_policy p ON sc.policy_id = p.id " +
                "WHERE p.organization_id = ? " +
                "ORDER BY sc.detected_at DESC",
                organizationId);
    }

    @Override
    public void updateConflictStatus(String id, String status, String resolution, long resolvedAt) {
        jdbcTemplate.update(
                "UPDATE identity_sod_conflict SET status = ?, resolution = ?, resolved_at = ? WHERE id = ?",
                status, resolution, resolvedAt, id);
    }

    // ── Exceptions ───────────────────────────────────────────────

    @Override
    public void insertException(String id, String conflictId, String reason,
                                String compensatingControl, String approvedBy,
                                long validFrom, long expiresAt) {
        jdbcTemplate.update(
                "INSERT INTO identity_sod_exception (id, conflict_id, reason, compensating_control, " +
                "approved_by, valid_from, expires_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                id, conflictId, reason, compensatingControl, approvedBy, validFrom, expiresAt
        );
    }

    // ── Grants (for SoD detection) ───────────────────────────────

    @Override
    public List<Map<String, Object>> findActiveGrantsBySubject(String subjectId) {
        return jdbcTemplate.queryForList(
                "SELECT * FROM identity_grant WHERE subject_id = ? AND status = 'ACTIVE'",
                subjectId);
    }
}
