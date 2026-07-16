package com.github.houbb.core.identity.api.publicapi.controller;

import com.github.houbb.core.identity.application.domain.PrivilegedActivation;
import com.github.houbb.core.identity.application.service.PrivilegedAccessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Public API for privileged access management.
 */
@RestController
@RequestMapping("/api/v1/identity/me")
public class PrivilegedAccessController {

    private final PrivilegedAccessService privilegedAccessService;

    public PrivilegedAccessController(PrivilegedAccessService privilegedAccessService) {
        this.privilegedAccessService = privilegedAccessService;
    }

    @GetMapping("/eligible-access")
    public ResponseEntity<Map<String, Object>> listEligibleAccess() {
        // TODO: 查询用户有资格激活的特权角色（来自 GRANT 表中有 Privileged Activation 权限的项）
        return ResponseEntity.ok(Map.of("eligibleRoles", List.of(),
                "message", "我的可激活特权"));
    }

    @PostMapping("/privileged-activations")
    public ResponseEntity<Map<String, Object>> activate(@RequestBody Map<String, Object> body) {
        String currentUserId = "current-user";
        String organizationId = (String) body.get("organizationId");
        String roleId = (String) body.get("roleId");
        String reason = (String) body.get("reason");
        String ticketReference = (String) body.get("ticketReference");
        String authenticationLevel = (String) body.getOrDefault("authenticationLevel", "AUTH_LEVEL_1");
        long durationSeconds = body.get("durationSeconds") instanceof Number
                ? ((Number) body.get("durationSeconds")).longValue() : 3600;

        PrivilegedActivation activation = privilegedAccessService.activate(
                currentUserId, organizationId, roleId, reason, ticketReference,
                authenticationLevel, durationSeconds);
        return ResponseEntity.status(201).body(toMap(activation));
    }

    @GetMapping("/privileged-activations")
    public ResponseEntity<Map<String, Object>> listMyActivations() {
        String currentUserId = "current-user";
        List<PrivilegedActivation> activations = privilegedAccessService.listByUser(currentUserId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (PrivilegedActivation act : activations) {
            result.add(toMap(act));
        }
        return ResponseEntity.ok(Map.of("activations", result, "total", result.size()));
    }

    @PostMapping("/privileged-activations/{activationId}/end")
    public ResponseEntity<Map<String, Object>> endActivation(@PathVariable String activationId) {
        String currentUserId = "current-user";
        privilegedAccessService.end(activationId, currentUserId);
        return ResponseEntity.ok(Map.of("message", "特权已结束", "activationId", activationId));
    }

    private Map<String, Object> toMap(PrivilegedActivation act) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", act.getId());
        map.put("userId", act.getUserId());
        map.put("organizationId", act.getOrganizationId());
        map.put("roleId", act.getRoleId());
        map.put("reason", act.getReason());
        map.put("ticketReference", act.getTicketReference());
        map.put("status", act.getStatus());
        map.put("authenticationLevel", act.getAuthenticationLevel());
        map.put("activatedAt", act.getActivatedAt());
        map.put("expiresAt", act.getExpiresAt());
        if (act.getExpiresAt() > 0 && act.getActivatedAt() > 0) {
            map.put("remainingSeconds", Math.max(0, (act.getExpiresAt() - System.currentTimeMillis()) / 1000));
        }
        return map;
    }
}
