package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.port.AccessReviewDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Access Review 服务 — 创建审查活动、管理审查项、处理审查决定。
 */
public class AccessReviewService {

    private static final Logger log = LoggerFactory.getLogger(AccessReviewService.class);

    private final AccessReviewDataRepository repo;

    public AccessReviewService(AccessReviewDataRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public Map<String, Object> createCampaign(String organizationId, String name, String campaignType,
                                               String scopeJson, String reviewerPolicyJson,
                                               long startsAt, long dueAt, String createdBy) {
        long now = System.currentTimeMillis();
        String id = UUID.randomUUID().toString();

        repo.insertCampaign(id, organizationId, name, campaignType, scopeJson, reviewerPolicyJson,
                startsAt, dueAt, createdBy, now);

        log.info("Created access review campaign: {} in org {}", name, organizationId);
        return Map.of("id", id, "name", name, "status", "DRAFT");
    }

    @Transactional
    public void launchCampaign(String campaignId) {
        long now = System.currentTimeMillis();
        repo.updateCampaignStatus(campaignId, "ACTIVE", now);

        Map<String, Object> campaign = repo.findCampaignById(campaignId);
        if (campaign == null) return;

        String orgId = (String) campaign.get("organization_id");
        List<Map<String, Object>> grants = repo.findActiveGrantsByOrg(orgId);

        for (Map<String, Object> grant : grants) {
            String itemId = UUID.randomUUID().toString();
            repo.insertItem(itemId, campaignId,
                    (String) grant.get("subject_type"), (String) grant.get("subject_id"),
                    (String) grant.get("entitlement_id"), (String) grant.get("id"),
                    "LOW", now);
        }

        log.info("Launched campaign {} with {} review items", campaignId, grants.size());
    }

    @Transactional
    public void decide(String reviewItemId, String reviewerUserId, String decision,
                        String reason, long newExpiryAt) {
        long now = System.currentTimeMillis();
        String id = UUID.randomUUID().toString();

        repo.insertDecision(id, reviewItemId, reviewerUserId, decision, reason,
                newExpiryAt > 0 ? newExpiryAt : null, now);

        String itemStatus = switch (decision) {
            case "CERTIFY" -> "CERTIFIED";
            case "REVOKE" -> "REVOKED";
            case "MODIFY" -> "MODIFIED";
            default -> "PENDING";
        };
        repo.updateItemStatus(reviewItemId, itemStatus, now);

        if ("REVOKE".equals(decision)) {
            List<Map<String, Object>> items = repo.findItemsByCampaign(null); // fallback — find grant from item
            for (Map<String, Object> item : items) {
                if (reviewItemId.equals(item.get("id"))) {
                    String grantId = (String) item.get("grant_id");
                    if (grantId != null) {
                        repo.revokeGrant(grantId, reviewerUserId, now, "Access Review: " + reason, now);
                    }
                    break;
                }
            }
        }

        log.info("Review decision recorded: item={}, decision={}", reviewItemId, decision);
    }

    @Transactional
    public void completeCampaign(String campaignId) {
        long now = System.currentTimeMillis();
        repo.updateCampaignStatus(campaignId, "COMPLETED", now);
        log.info("Completed access review campaign: {}", campaignId);
    }

    public List<Map<String, Object>> listCampaigns(String organizationId) {
        return repo.findCampaignsByOrg(organizationId);
    }

    public Map<String, Object> getCampaignDetail(String campaignId) {
        Map<String, Object> campaign = repo.findCampaignById(campaignId);
        List<Map<String, Object>> items = repo.findItemsByCampaign(campaignId);
        campaign.put("items", items);
        campaign.put("itemCount", items.size());
        return campaign;
    }

    public List<Map<String, Object>> listReviewItems(String campaignId) {
        return repo.findItemsByCampaignWithDecisions(campaignId);
    }

    public static class ServiceException extends RuntimeException {
        private final String errorCode;
        public ServiceException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }
        public String getErrorCode() { return errorCode; }
    }
}
