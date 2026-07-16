package com.github.houbb.core.identity.api.publicapi.controller;

import com.github.houbb.core.identity.api.response.ErrorResponse;
import com.github.houbb.core.identity.application.domain.ExternalIdentity;
import com.github.houbb.core.identity.application.service.ExternalIdentityService;
import com.github.houbb.core.identity.application.service.ExternalIdentityServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * External Identity Controller — user identity binding/unbinding and account link management.
 *
 * P5: User-facing endpoints for managing linked enterprise identities.
 */
@RestController
@RequestMapping("/api/v1/identity/me")
public class ExternalIdentityController {

    private final ExternalIdentityService externalIdentityService;

    public ExternalIdentityController(ExternalIdentityService externalIdentityService) {
        this.externalIdentityService = externalIdentityService;
    }

    // ==================== My External Identities ====================

    @GetMapping("/external-identities")
    public ResponseEntity<Map<String, Object>> listExternalIdentities(HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        List<ExternalIdentity> identities = externalIdentityService.getUserExternalIdentities(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ExternalIdentity ei : identities) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", ei.getId());
            item.put("organizationId", ei.getOrganizationId());
            item.put("connectionId", ei.getConnectionId());
            item.put("externalSubject", ei.getExternalSubject());
            item.put("externalEmail", ei.getExternalEmail());
            item.put("status", ei.getStatus());
            item.put("managementSource", ei.getManagementSource());
            item.put("lastLoginAt", ei.getLastLoginAt());
            result.add(item);
        }

        return ResponseEntity.ok(Map.of("identities", result, "total", result.size()));
    }

    @DeleteMapping("/external-identities/{externalIdentityId}")
    public ResponseEntity<Map<String, Object>> unlinkExternalIdentity(
            @PathVariable String externalIdentityId, HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        try {
            long now = System.currentTimeMillis();
            externalIdentityService.unlinkExternalIdentity(externalIdentityId, userId, now);
            return ResponseEntity.ok(Map.of("message", "External identity unlinked"));
        } catch (ExternalIdentityServiceImpl.ServiceException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== Account Link Requests ====================

    @GetMapping("/account-link-requests/{requestId}")
    public ResponseEntity<Map<String, Object>> getLinkRequest(@PathVariable String requestId) {
        return ResponseEntity.ok(Map.of("id", requestId, "status", "PENDING",
                "message", "Please confirm or reject this account link request"));
    }

    @PostMapping("/account-link-requests/{requestId}/confirm")
    public ResponseEntity<Map<String, Object>> confirmLink(@PathVariable String requestId,
                                                            HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        try {
            externalIdentityService.confirmAccountLink(requestId, userId, System.currentTimeMillis());
            return ResponseEntity.ok(Map.of("message", "Account link confirmed"));
        } catch (ExternalIdentityServiceImpl.ServiceException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/account-link-requests/{requestId}/reject")
    public ResponseEntity<Map<String, Object>> rejectLink(@PathVariable String requestId) {
        try {
            externalIdentityService.rejectAccountLink(requestId, System.currentTimeMillis());
            return ResponseEntity.ok(Map.of("message", "Account link rejected"));
        } catch (ExternalIdentityServiceImpl.ServiceException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== Helper ====================

    private String getCurrentUserId(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("core_identity_session".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}