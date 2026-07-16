package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.port.AccessReviewDataRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class JdbcAccessReviewDataRepository implements AccessReviewDataRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAccessReviewDataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ── Campaign ─────────────────────────────────────────────────

    @Override
    public void insertCampaign(String id, String organizationId, String name, String campaignType,
                               String scopeJson, String reviewerPolicyJson, long startsAt, long dueAt,
                               String createdBy, long now) {
        jdbcTemplate.update(
                "INSERT INTO identity_access_review_campaign (id, organization_id, name, campaign_type, " +
                "scope_json, reviewer_policy_json, starts_at, due_at, status, created_by, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'PENDING', ?, ?, ?)",
                id, organizationId, name, campaignType, scopeJson, reviewerPolicyJson,
                startsAt, dueAt, createdBy, now, now
        );
    }

    @Override
    public void updateCampaignStatus(String id, String status, long now) {
        jdbcTemplate.update(
                "UPDATE identity_access_review_campaign SET status = ?, updated_at = ? WHERE id = ?",
                status, now, id);
    }

    @Override
    public List<Map<String, Object>> findCampaignsByOrg(String organizationId) {
        return jdbcTemplate.queryForList(
                "SELECT * FROM identity_access_review_campaign WHERE organization_id = ? ORDER BY starts_at DESC",
                organizationId);
    }

    @Override
    public Map<String, Object> findCampaignById(String campaignId) {
        return jdbcTemplate.queryForMap(
                "SELECT * FROM identity_access_review_campaign WHERE id = ?",
                campaignId);
    }

    // ── Items ────────────────────────────────────────────────────

    @Override
    public void insertItem(String id, String campaignId, String subjectType, String subjectId,
                           String entitlementId, String grantId, String riskLevel, long now) {
        jdbcTemplate.update(
                "INSERT INTO identity_access_review_item (id, campaign_id, subject_type, subject_id, " +
                "entitlement_id, grant_id, risk_level, status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING', ?, ?)",
                id, campaignId, subjectType, subjectId, entitlementId, grantId, riskLevel, now, now
        );
    }

    @Override
    public void updateItemStatus(String id, String status, long now) {
        jdbcTemplate.update(
                "UPDATE identity_access_review_item SET status = ?, updated_at = ? WHERE id = ?",
                status, now, id);
    }

    @Override
    public List<Map<String, Object>> findItemsByCampaign(String campaignId) {
        return jdbcTemplate.queryForList(
                "SELECT * FROM identity_access_review_item WHERE campaign_id = ? ORDER BY risk_level DESC",
                campaignId);
    }

    @Override
    public List<Map<String, Object>> findItemsByCampaignWithDecisions(String campaignId) {
        return jdbcTemplate.queryForList(
                "SELECT i.*, d.id AS decision_id, d.reviewer_user_id, d.decision, d.reason, " +
                "d.new_expiry_at, d.created_at AS decision_at " +
                "FROM identity_access_review_item i " +
                "LEFT JOIN identity_access_review_decision d ON i.id = d.review_item_id " +
                "WHERE i.campaign_id = ? " +
                "ORDER BY i.risk_level DESC",
                campaignId);
    }

    // ── Decisions ────────────────────────────────────────────────

    @Override
    public void insertDecision(String id, String reviewItemId, String reviewerUserId,
                               String decision, String reason, Long newExpiryAt, long now) {
        jdbcTemplate.update(
                "INSERT INTO identity_access_review_decision (id, review_item_id, reviewer_user_id, " +
                "decision, reason, new_expiry_at, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                id, reviewItemId, reviewerUserId, decision, reason, newExpiryAt, now
        );
    }

    // ── Grants (for review) ──────────────────────────────────────

    @Override
    public List<Map<String, Object>> findActiveGrantsByOrg(String organizationId) {
        return jdbcTemplate.queryForList(
                "SELECT * FROM identity_grant WHERE organization_id = ? AND status = 'ACTIVE'",
                organizationId);
    }

    @Override
    public void revokeGrant(String grantId, String revokedBy, long revokedAt, String reason, long now) {
        jdbcTemplate.update(
                "UPDATE identity_grant SET status = 'REVOKED', revoked_by = ?, revoked_at = ?, " +
                "revoke_reason = ?, updated_at = ? WHERE id = ?",
                revokedBy, revokedAt, reason, now, grantId);
    }
}
