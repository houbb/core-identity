package com.github.houbb.core.identity.api.publicapi.controller;

import com.github.houbb.core.identity.application.domain.AccessRequest;
import com.github.houbb.core.identity.application.domain.ApprovalDecision;
import com.github.houbb.core.identity.application.service.AccessRequestService;
import com.github.houbb.core.identity.application.service.ApprovalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Public API for access requests and approvals.
 */
@RestController
@RequestMapping("/api/v1/identity")
public class AccessRequestController {

    private final AccessRequestService requestService;
    private final ApprovalService approvalService;

    public AccessRequestController(AccessRequestService requestService,
                                   ApprovalService approvalService) {
        this.requestService = requestService;
        this.approvalService = approvalService;
    }

    // ========== 我的访问申请 ==========

    @GetMapping("/me/access-requests")
    public ResponseEntity<Map<String, Object>> listMyRequests() {
        // TODO: get current user from session
        String currentUserId = "current-user";
        List<AccessRequest> requests = requestService.listByRequester(currentUserId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (AccessRequest req : requests) {
            result.add(toRequestMap(req));
        }
        return ResponseEntity.ok(Map.of("requests", result, "total", result.size()));
    }

    @PostMapping("/me/access-requests")
    public ResponseEntity<Map<String, Object>> submitRequest(@RequestBody Map<String, Object> body) {
        // TODO: get current user from session
        String currentUserId = "current-user";
        String organizationId = (String) body.get("organizationId");
        String accessPackageId = (String) body.get("accessPackageId");
        String businessReason = (String) body.get("businessReason");
        String ticketReference = (String) body.get("ticketReference");
        long requestedStartAt = body.get("requestedStartAt") instanceof Number
                ? ((Number) body.get("requestedStartAt")).longValue() : 0;
        long requestedEndAt = body.get("requestedEndAt") instanceof Number
                ? ((Number) body.get("requestedEndAt")).longValue() : 0;

        AccessRequest request = requestService.submit(currentUserId, organizationId,
                accessPackageId, businessReason, ticketReference, requestedStartAt, requestedEndAt);
        return ResponseEntity.status(201).body(toRequestMap(request));
    }

    @GetMapping("/me/access-requests/{requestId}")
    public ResponseEntity<Map<String, Object>> getMyRequest(@PathVariable String requestId) {
        AccessRequest request = requestService.getById(requestId);
        return ResponseEntity.ok(toRequestMap(request));
    }

    @PostMapping("/me/access-requests/{requestId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelMyRequest(@PathVariable String requestId) {
        String currentUserId = "current-user";
        requestService.cancel(requestId, currentUserId);
        return ResponseEntity.ok(Map.of("message", "申请已取消", "requestId", requestId));
    }

    // ========== 我的审批 ==========

    @PostMapping("/me/approvals/{approvalStepId}/approve")
    public ResponseEntity<Map<String, Object>> approve(
            @PathVariable String approvalStepId,
            @RequestBody Map<String, String> body) {
        String currentUserId = "current-user";
        String reason = body.getOrDefault("reason", "");
        ApprovalService.ApprovalStatus status = approvalService.decide(
                approvalStepId, currentUserId, "APPROVED", reason);
        return ResponseEntity.ok(Map.of(
                "instanceId", status.instanceId(),
                "status", status.status(),
                "decision", "APPROVED",
                "isFullyApproved", status.isFullyApproved()
        ));
    }

    @PostMapping("/me/approvals/{approvalStepId}/reject")
    public ResponseEntity<Map<String, Object>> reject(
            @PathVariable String approvalStepId,
            @RequestBody Map<String, String> body) {
        String currentUserId = "current-user";
        String reason = body.getOrDefault("reason", "");
        ApprovalService.ApprovalStatus status = approvalService.decide(
                approvalStepId, currentUserId, "REJECTED", reason);
        return ResponseEntity.ok(Map.of(
                "instanceId", status.instanceId(),
                "status", status.status(),
                "decision", "REJECTED"
        ));
    }

    // ========== Admin: 组织申请管理 ==========

    @GetMapping("/organizations/{organizationId}/access-requests")
    public ResponseEntity<Map<String, Object>> listOrgRequests(@PathVariable String organizationId) {
        List<AccessRequest> requests = requestService.listByOrganization(organizationId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (AccessRequest req : requests) {
            result.add(toRequestMap(req));
        }
        return ResponseEntity.ok(Map.of("requests", result, "total", result.size()));
    }

    private Map<String, Object> toRequestMap(AccessRequest req) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", req.getId());
        map.put("requesterUserId", req.getRequesterUserId());
        map.put("organizationId", req.getOrganizationId());
        map.put("accessPackageId", req.getAccessPackageId());
        map.put("businessReason", req.getBusinessReason());
        map.put("ticketReference", req.getTicketReference());
        map.put("status", req.getStatus());
        map.put("riskLevel", req.getRiskLevel());
        map.put("sodResult", req.getSodResult());
        map.put("submittedAt", req.getSubmittedAt());
        map.put("completedAt", req.getCompletedAt());
        map.put("createdAt", req.getCreatedAt());
        return map;
    }
}
