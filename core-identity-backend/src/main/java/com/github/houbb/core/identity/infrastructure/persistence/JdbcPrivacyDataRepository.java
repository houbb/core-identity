package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.port.PrivacyDataRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class JdbcPrivacyDataRepository implements PrivacyDataRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPrivacyDataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ── Privacy Request ──────────────────────────────────────────

    @Override
    public void insertRequest(String id, String userId, String organizationId, String requestType,
                              String jurisdiction, long submittedAt, long dueAt, long now) {
        jdbcTemplate.update(
                "INSERT INTO identity_privacy_request (id, user_id, organization_id, request_type, " +
                "jurisdiction, submitted_at, due_at, status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING', ?, ?)",
                id, userId, organizationId, requestType, jurisdiction, submittedAt, dueAt, now, now
        );
    }

    @Override
    public void updateRequestStatus(String id, String status, String verificationLevel, long now) {
        jdbcTemplate.update(
                "UPDATE identity_privacy_request SET status = ?, verification_level = ?, updated_at = ? WHERE id = ?",
                status, verificationLevel, now, id);
    }

    @Override
    public void updateRequestCompleted(String id, String status, long now) {
        jdbcTemplate.update(
                "UPDATE identity_privacy_request SET status = ?, completed_at = ?, updated_at = ? WHERE id = ?",
                status, now, now, id);
    }

    @Override
    public void updateRequestRejected(String id, String reason, long now) {
        jdbcTemplate.update(
                "UPDATE identity_privacy_request SET status = 'REJECTED', rejection_reason = ?, updated_at = ? WHERE id = ?",
                reason, now, id);
    }

    @Override
    public List<Map<String, Object>> findRequestsByStatus(String status) {
        return jdbcTemplate.queryForList(
                "SELECT * FROM identity_privacy_request WHERE status = ? ORDER BY due_at",
                status);
    }

    @Override
    public Map<String, Object> findRequestById(String requestId) {
        return jdbcTemplate.queryForMap(
                "SELECT * FROM identity_privacy_request WHERE id = ?",
                requestId);
    }

    @Override
    public List<Map<String, Object>> findTasksByRequestId(String requestId) {
        return jdbcTemplate.queryForList(
                "SELECT * FROM identity_privacy_request_task WHERE privacy_request_id = ? ORDER BY created_at",
                requestId);
    }

    // ── Tasks ────────────────────────────────────────────────────

    @Override
    public void insertTask(String id, String privacyRequestId, String targetService,
                           String taskType, long now) {
        jdbcTemplate.update(
                "INSERT INTO identity_privacy_request_task (id, privacy_request_id, target_service, " +
                "task_type, status, created_at, updated_at) VALUES (?, ?, ?, ?, 'PENDING', ?, ?)",
                id, privacyRequestId, targetService, taskType, now, now
        );
    }

    // ── Retention Policy ─────────────────────────────────────────

    @Override
    public void insertRetentionPolicy(String id, String organizationId, String dataCategory,
                                      String triggerType, long retentionSeconds, String expirationAction,
                                      String jurisdiction, int priority, long now) {
        jdbcTemplate.update(
                "INSERT INTO identity_retention_policy (id, organization_id, data_category, trigger_type, " +
                "retention_seconds, expiration_action, jurisdiction, priority, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, organizationId, dataCategory, triggerType, retentionSeconds,
                expirationAction, jurisdiction, priority, now, now
        );
    }

    @Override
    public List<Map<String, Object>> findRetentionPoliciesByOrg(String organizationId) {
        return jdbcTemplate.queryForList(
                "SELECT * FROM identity_retention_policy WHERE organization_id = ? ORDER BY priority DESC",
                organizationId);
    }

    // ── Legal Hold ───────────────────────────────────────────────

    @Override
    public void insertLegalHold(String id, String organizationId, String caseReference, String name,
                                String reason, long effectiveAt, long reviewAt, String createdBy, long now) {
        jdbcTemplate.update(
                "INSERT INTO identity_legal_hold (id, organization_id, case_reference, name, reason, " +
                "effective_at, review_at, created_by, status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE', ?, ?)",
                id, organizationId, caseReference, name, reason,
                effectiveAt, reviewAt, createdBy, now, now
        );
    }

    @Override
    public void insertLegalHoldScope(String id, String legalHoldId, String scopeType,
                                     String scopeReference, String dataCategory) {
        jdbcTemplate.update(
                "INSERT INTO identity_legal_hold_scope (id, legal_hold_id, scope_type, scope_reference, " +
                "data_category) VALUES (?, ?, ?, ?, ?)",
                id, legalHoldId, scopeType, scopeReference, dataCategory
        );
    }

    @Override
    public void releaseLegalHold(String id, long now) {
        jdbcTemplate.update(
                "UPDATE identity_legal_hold SET status = 'RELEASED', released_at = ?, updated_at = ? WHERE id = ?",
                now, now, id);
    }

    @Override
    public List<Map<String, Object>> findLegalHoldsByOrg(String organizationId) {
        return jdbcTemplate.queryForList(
                "SELECT * FROM identity_legal_hold WHERE organization_id = ? ORDER BY effective_at DESC",
                organizationId);
    }

    @Override
    public int countActiveLegalHoldByScope(String scopeReference, String dataCategory) {
        //noinspection DataFlowIssue
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM identity_legal_hold_scope s " +
                "JOIN identity_legal_hold h ON s.legal_hold_id = h.id " +
                "WHERE s.scope_reference = ? AND s.data_category = ? AND h.status = 'ACTIVE'",
                Integer.class, scopeReference, dataCategory);
    }

    // ── Processing Activity ──────────────────────────────────────

    @Override
    public void insertProcessingActivity(String id, String organizationId, String activityCode,
                                         String name, String purpose, String controller, long now) {
        jdbcTemplate.update(
                "INSERT INTO identity_processing_activity (id, organization_id, activity_code, name, " +
                "purpose, controller, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                id, organizationId, activityCode, name, purpose, controller, now, now
        );
    }

    @Override
    public List<Map<String, Object>> findProcessingActivitiesByOrg(String organizationId) {
        return jdbcTemplate.queryForList(
                "SELECT * FROM identity_processing_activity WHERE organization_id = ? ORDER BY name",
                organizationId);
    }
}
