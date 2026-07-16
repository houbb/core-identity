package com.github.houbb.core.identity.api.publicapi.controller;

import com.github.houbb.core.identity.application.service.AccessReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/identity")
public class AccessReviewController {

    private final AccessReviewService reviewService;

    public AccessReviewController(AccessReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // ========== Admin: Campaign Management ==========

    @GetMapping("/organizations/{organizationId}/access-reviews")
    public ResponseEntity<Map<String, Object>> listCampaigns(@PathVariable String organizationId) {
        List<Map<String, Object>> campaigns = reviewService.listCampaigns(organizationId);
        return ResponseEntity.ok(Map.of("campaigns", campaigns, "total", campaigns.size()));
    }

    @PostMapping("/organizations/{organizationId}/access-reviews")
    public ResponseEntity<Map<String, Object>> createCampaign(
            @PathVariable String organizationId,
            @RequestBody Map<String, Object> body) {
        Map<String, Object> campaign = reviewService.createCampaign(organizationId,
                (String) body.get("name"),
                (String) body.getOrDefault("campaignType", "USER_ACCESS"),
                (String) body.get("scopeJson"),
                (String) body.get("reviewerPolicyJson"),
                body.get("startsAt") instanceof Number ? ((Number) body.get("startsAt")).longValue() : 0,
                body.get("dueAt") instanceof Number ? ((Number) body.get("dueAt")).longValue() : 0,
                (String) body.get("createdBy"));
        return ResponseEntity.status(201).body(campaign);
    }

    @PostMapping("/organizations/{organizationId}/access-reviews/{campaignId}/launch")
    public ResponseEntity<Map<String, Object>> launchCampaign(
            @PathVariable String organizationId,
            @PathVariable String campaignId) {
        reviewService.launchCampaign(campaignId);
        return ResponseEntity.ok(Map.of("message", "审查活动已启动", "campaignId", campaignId));
    }

    @PostMapping("/organizations/{organizationId}/access-reviews/{campaignId}/close")
    public ResponseEntity<Map<String, Object>> closeCampaign(
            @PathVariable String organizationId,
            @PathVariable String campaignId) {
        reviewService.completeCampaign(campaignId);
        return ResponseEntity.ok(Map.of("message", "审查活动已完成", "campaignId", campaignId));
    }

    @GetMapping("/organizations/{organizationId}/access-reviews/{campaignId}")
    public ResponseEntity<Map<String, Object>> getCampaignDetail(
            @PathVariable String organizationId,
            @PathVariable String campaignId) {
        return ResponseEntity.ok(reviewService.getCampaignDetail(campaignId));
    }

    @GetMapping("/organizations/{organizationId}/access-reviews/{campaignId}/items")
    public ResponseEntity<Map<String, Object>> listReviewItems(
            @PathVariable String organizationId,
            @PathVariable String campaignId) {
        List<Map<String, Object>> items = reviewService.listReviewItems(campaignId);
        return ResponseEntity.ok(Map.of("items", items, "total", items.size()));
    }

    // ========== Me: My Reviews ==========

    @PostMapping("/me/access-reviews/{reviewItemId}/decisions")
    public ResponseEntity<Map<String, Object>> decide(
            @PathVariable String reviewItemId,
            @RequestBody Map<String, Object> body) {
        String currentUserId = "current-user";
        reviewService.decide(reviewItemId, currentUserId,
                (String) body.get("decision"),
                (String) body.getOrDefault("reason", ""),
                body.get("newExpiryAt") instanceof Number ? ((Number) body.get("newExpiryAt")).longValue() : 0);
        return ResponseEntity.ok(Map.of("message", "审查决定已记录", "itemId", reviewItemId));
    }
}
