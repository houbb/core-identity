package com.github.houbb.core.identity.application.port;

import java.util.List;
import java.util.Map;

/**
 * Repository for access review tables: identity_access_review_campaign, identity_access_review_item, identity_access_review_decision.
 */
public interface AccessReviewDataRepository {

    // Campaign
    void insertCampaign(String id, String organizationId, String name, String campaignType,
                        String scopeJson, String reviewerPolicyJson, long startsAt, long dueAt,
                        String createdBy, long now);

    void updateCampaignStatus(String id, String status, long now);

    List<Map<String, Object>> findCampaignsByOrg(String organizationId);

    Map<String, Object> findCampaignById(String campaignId);

    // Items
    void insertItem(String id, String campaignId, String subjectType, String subjectId,
                    String entitlementId, String grantId, String riskLevel, long now);

    void updateItemStatus(String id, String status, long now);

    List<Map<String, Object>> findItemsByCampaign(String campaignId);

    List<Map<String, Object>> findItemsByCampaignWithDecisions(String campaignId);

    // Decisions
    void insertDecision(String id, String reviewItemId, String reviewerUserId, String decision,
                        String reason, Long newExpiryAt, long now);

    // Grants (for review)
    List<Map<String, Object>> findActiveGrantsByOrg(String organizationId);

    void revokeGrant(String grantId, String revokedBy, long revokedAt, String reason, long now);
}