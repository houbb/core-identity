package com.github.houbb.core.identity.api.publicapi.controller;

import com.github.houbb.core.identity.api.response.ErrorResponse;
import com.github.houbb.core.identity.application.service.FederationService;
import com.github.houbb.core.identity.application.service.FederationServiceImpl.FederationException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Organization SSO Controller — domain verification, connection CRUD, SSO policy management.
 *
 * P5: Organization-scoped SSO administration endpoints.
 * Requires organization membership authentication via session cookie.
 */
@RestController
@RequestMapping("/api/v1/identity/organizations/{organizationId}")
public class OrganizationSsoController {

    private final FederationService federationService;

    public OrganizationSsoController(FederationService federationService) {
        this.federationService = federationService;
    }

    // ==================== Domain Verification ====================

    @PostMapping("/domains")
    public ResponseEntity<?> addDomain(@PathVariable String organizationId,
                                        @RequestBody Map<String, String> body,
                                        HttpServletRequest request) {
        try {
            String domainName = body.get("domainName");
            if (domainName == null || domainName.isEmpty()) {
                throw new FederationException("IDENTITY_INVALID_REQUEST", "domainName is required");
            }

            String userId = getCurrentUserId(request);
            FederationService.DomainVerificationResult result = federationService.initiateDomainVerification(
                    organizationId, domainName, userId, request.getHeader("X-Request-ID"));

            return ResponseEntity.ok(Map.of(
                    "domainId", result.domainId(),
                    "status", result.status(),
                    "message", result.message()
            ));
        } catch (FederationException e) {
            return error(e, request);
        }
    }

    @PostMapping("/domains/{domainId}/verify")
    public ResponseEntity<?> verifyDomain(@PathVariable String organizationId,
                                          @PathVariable String domainId,
                                          HttpServletRequest request) {
        try {
            FederationService.DomainVerificationResult result = federationService.checkDomainVerification(
                    organizationId, domainId, request.getHeader("X-Request-ID"));

            return ResponseEntity.ok(Map.of(
                    "domainId", result.domainId(),
                    "status", result.status(),
                    "message", result.message()
            ));
        } catch (FederationException e) {
            return error(e, request);
        }
    }

    // ==================== Federation Connections ====================

    @PostMapping("/federation-connections")
    public ResponseEntity<?> createConnection(@PathVariable String organizationId,
                                               @RequestBody Map<String, String> body,
                                               HttpServletRequest request) {
        try {
            String connectionType = body.getOrDefault("connectionType", "OIDC");
            String name = body.get("name");
            String userId = getCurrentUserId(request);

            String connectionId = federationService.createConnection(
                    organizationId, connectionType, name, userId, request.getHeader("X-Request-ID"));

            return ResponseEntity.ok(Map.of("connectionId", connectionId, "message", "Connection created"));
        } catch (FederationException e) {
            return error(e, request);
        }
    }

    @PostMapping("/federation-connections/{connectionId}/activate")
    public ResponseEntity<?> activateConnection(@PathVariable String organizationId,
                                                 @PathVariable String connectionId,
                                                 HttpServletRequest request) {
        try {
            federationService.activateConnection(organizationId, connectionId, request.getHeader("X-Request-ID"));
            return ResponseEntity.ok(Map.of("message", "Connection activated"));
        } catch (FederationException e) {
            return error(e, request);
        }
    }

    @PostMapping("/federation-connections/{connectionId}/suspend")
    public ResponseEntity<?> suspendConnection(@PathVariable String organizationId,
                                                @PathVariable String connectionId,
                                                HttpServletRequest request) {
        try {
            federationService.suspendConnection(organizationId, connectionId, request.getHeader("X-Request-ID"));
            return ResponseEntity.ok(Map.of("message", "Connection suspended"));
        } catch (FederationException e) {
            return error(e, request);
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

    private ResponseEntity<ErrorResponse> error(FederationException e, HttpServletRequest request) {
        int status = switch (e.getErrorCode()) {
            case "IDENTITY_FEDERATION_CONNECTION_NOT_FOUND",
                 "IDENTITY_DOMAIN_NOT_VERIFIED" -> 404;
            case "IDENTITY_DOMAIN_CONFLICT" -> 409;
            case "IDENTITY_FEDERATION_CONNECTION_NOT_ACTIVE" -> 403;
            default -> 500;
        };
        ErrorResponse err = ErrorResponse.of(status, "SSO error", e.getMessage(),
                e.getErrorCode(), request.getHeader("X-Request-ID"));
        return ResponseEntity.status(status).body(err);
    }
}
