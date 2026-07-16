package com.github.houbb.core.identity.api.publicapi.controller;

import com.github.houbb.core.identity.application.domain.SodPolicy;
import com.github.houbb.core.identity.application.service.SodService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/identity/organizations/{organizationId}")
public class SodController {

    private final SodService sodService;

    public SodController(SodService sodService) {
        this.sodService = sodService;
    }

    @GetMapping("/sod-policies")
    public ResponseEntity<Map<String, Object>> listPolicies(@PathVariable String organizationId) {
        List<SodPolicy> policies = sodService.listPolicies(organizationId);
        return ResponseEntity.ok(Map.of("policies", policies, "total", policies.size()));
    }

    @PostMapping("/sod-policies")
    public ResponseEntity<Map<String, Object>> createPolicy(
            @PathVariable String organizationId,
            @RequestBody Map<String, String> body) {
        SodPolicy policy = sodService.createPolicy(organizationId,
                body.get("name"), body.get("enforcementMode"), body.get("ownerUserId"));
        return ResponseEntity.status(201).body(Map.of("policy", policy));
    }

    @PostMapping("/sod-policies/{policyId}/items")
    public ResponseEntity<Map<String, Object>> addPolicyItem(
            @PathVariable String organizationId,
            @PathVariable String policyId,
            @RequestBody Map<String, String> body) {
        sodService.addPolicyItem(policyId,
                body.get("leftEntitlementId"), body.get("rightEntitlementId"),
                body.get("riskLevel"));
        return ResponseEntity.status(201).body(Map.of("message", "策略项已添加"));
    }

    @GetMapping("/sod-conflicts")
    public ResponseEntity<Map<String, Object>> listConflicts(@PathVariable String organizationId) {
        List<Map<String, Object>> conflicts = sodService.listConflicts(organizationId);
        return ResponseEntity.ok(Map.of("conflicts", conflicts, "total", conflicts.size()));
    }

    @PostMapping("/sod-conflicts/{conflictId}/accept-risk")
    public ResponseEntity<Map<String, Object>> acceptRisk(
            @PathVariable String organizationId,
            @PathVariable String conflictId,
            @RequestBody Map<String, Object> body) {
        String reason = (String) body.getOrDefault("reason", "风险已接受");
        String compensatingControl = (String) body.get("compensatingControl");
        String approvedBy = (String) body.get("approvedBy");
        long durationSeconds = body.get("durationSeconds") instanceof Number
                ? ((Number) body.get("durationSeconds")).longValue() : 7776000; // 默认90天
        sodService.createException(conflictId, reason, compensatingControl, approvedBy, durationSeconds);
        return ResponseEntity.ok(Map.of("message", "风险例外已创建"));
    }

    @PostMapping("/sod-conflicts/{conflictId}/resolve")
    public ResponseEntity<Map<String, Object>> resolveConflict(
            @PathVariable String organizationId,
            @PathVariable String conflictId,
            @RequestBody Map<String, String> body) {
        sodService.resolveConflict(conflictId, body.getOrDefault("resolution", "REMEDIATED"));
        return ResponseEntity.ok(Map.of("message", "冲突已解决"));
    }
}
