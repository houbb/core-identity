package com.github.houbb.core.identity.api.publicapi.controller;

import com.github.houbb.core.identity.application.domain.SecurityPolicy;
import com.github.houbb.core.identity.application.service.AuthService;
import com.github.houbb.core.identity.application.service.SecurityPolicyService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Organization security policy controller.
 */
@RestController
@RequestMapping("/api/v1/identity/organizations/{organizationId}/security-policy")
public class OrganizationSecurityController {

    private final SecurityPolicyService policyService;
    private final AuthService authService;

    public OrganizationSecurityController(SecurityPolicyService policyService, AuthService authService) {
        this.policyService = policyService;
        this.authService = authService;
    }

    private String getCurrentUserId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if ("core_identity_session".equals(cookie.getName())) {
                return authService.introspectSession(cookie.getValue());
            }
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<?> getPolicy(@PathVariable String organizationId) {
        return policyService.findByOrganizationId(organizationId)
                .map(p -> ResponseEntity.ok(Map.of(
                        "id", p.getId(),
                        "name", p.getName(),
                        "status", p.getStatus(),
                        "minimumAuthLevel", p.getMinimumAuthLevel(),
                        "phishingResistantRequired", p.getPhishingResistantRequired() == 1,
                        "gracePeriodEndsAt", p.getGracePeriodEndsAt()
                )))
                .orElse(ResponseEntity.ok(Map.of("message", "No security policy configured")));
    }

    @PutMapping
    public ResponseEntity<?> updatePolicy(@PathVariable String organizationId,
                                           @RequestBody SecurityPolicy policy,
                                           HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        policy.setCreatedBy(userId);
        SecurityPolicy saved = policyService.createOrUpdate(organizationId, policy);
        return ResponseEntity.ok(Map.of("id", saved.getId(), "status", saved.getStatus()));
    }

    @PostMapping("/publish")
    public ResponseEntity<?> publishPolicy(@PathVariable String organizationId) {
        try {
            policyService.publish(organizationId);
            return ResponseEntity.ok(Map.of("message", "Policy published. Members have a 14-day grace period."));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/suspend")
    public ResponseEntity<?> suspendPolicy(@PathVariable String organizationId) {
        try {
            policyService.suspend(organizationId);
            return ResponseEntity.ok(Map.of("message", "Policy suspended"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }
}
