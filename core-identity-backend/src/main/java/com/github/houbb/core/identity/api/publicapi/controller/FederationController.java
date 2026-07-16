package com.github.houbb.core.identity.api.publicapi.controller;

import com.github.houbb.core.identity.api.response.ErrorResponse;
import com.github.houbb.core.identity.application.service.FederationService;
import com.github.houbb.core.identity.application.service.FederationServiceImpl.FederationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Federation SSO Controller — identity discovery, enterprise login entry, OIDC/SAML callbacks.
 *
 * P5: Public API for enterprise single sign-on flows.
 */
@RestController
public class FederationController {

    private final FederationService federationService;

    public FederationController(FederationService federationService) {
        this.federationService = federationService;
    }

    // ==================== Identity Discovery ====================

    @PostMapping("/api/v1/identity/auth/discovery")
    public ResponseEntity<?> discoverIdentity(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isEmpty()) {
            throw new FederationException("IDENTITY_INVALID_REQUEST", "Email is required");
        }

        FederationService.IdentityDiscoveryResult result = federationService.discoverIdentityByEmail(email);

        if ("LOCAL".equals(result.loginType())) {
            return ResponseEntity.ok(Map.of("loginType", "LOCAL"));
        }

        return ResponseEntity.ok(Map.of(
                "loginType", result.loginType(),
                "organizationName", result.organizationName(),
                "connectionKey", result.connectionKey(),
                "organizationId", result.organizationId()
        ));
    }

    // ==================== Federation Login ====================

    @GetMapping("/api/v1/identity/federation/{connectionKey}/login")
    public ResponseEntity<?> initiateFederationLogin(@PathVariable String connectionKey) {
        String redirectUrl = federationService.getConnectionLoginRedirect(connectionKey);
        return ResponseEntity.ok(Map.of(
                "redirectUrl", redirectUrl,
                "message", "Redirecting to enterprise identity provider"
        ));
    }

    // ==================== OIDC Callback ====================

    @GetMapping("/api/v1/identity/federation/oidc/{connectionKey}/callback")
    public ResponseEntity<?> oidcCallback(@PathVariable String connectionKey,
                                          @RequestParam("code") String code,
                                          @RequestParam("state") String state,
                                          @RequestParam(value = "error", required = false) String error,
                                          @RequestParam(value = "error_description", required = false) String errorDescription,
                                          HttpServletRequest request) {
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", error,
                    "error_description", errorDescription != null ? errorDescription : "IdP returned an error"
            ));
        }

        try {
            // OIDC callback will be handled by OidcRelyingPartyService
            // For now, delegate to FederationService
            return ResponseEntity.ok(Map.of(
                    "message", "SSO login in progress",
                    "connectionKey", connectionKey,
                    "state", state
            ));
        } catch (FederationException e) {
            return error(e, request.getHeader("X-Request-ID"));
        }
    }

    // ==================== SAML ACS ====================

    @PostMapping("/api/v1/identity/federation/saml/{connectionKey}/acs")
    public ResponseEntity<?> samlAcs(@PathVariable String connectionKey,
                                     @RequestParam("SAMLResponse") String samlResponse,
                                     @RequestParam(value = "RelayState", required = false) String relayState,
                                     HttpServletRequest request) {
        try {
            // SAML ACS will be handled by SamlServiceProviderService
            return ResponseEntity.ok(Map.of(
                    "message", "SAML SSO login in progress",
                    "connectionKey", connectionKey
            ));
        } catch (FederationException e) {
            return error(e, request.getHeader("X-Request-ID"));
        }
    }

    // ==================== Organization SSO Entry ====================

    @GetMapping("/sso/{organizationSlug}")
    public ResponseEntity<?> organizationSsoEntry(@PathVariable String organizationSlug) {
        String redirectUrl = federationService.getOrganizationSsoRedirect(organizationSlug);
        return ResponseEntity.ok(Map.of(
                "redirectUrl", redirectUrl,
                "message", "Redirecting to organization SSO"
        ));
    }

    // ==================== Helper ====================

    private ResponseEntity<ErrorResponse> error(FederationException e, String requestId) {
        int status = switch (e.getErrorCode()) {
            case "IDENTITY_FEDERATION_CONNECTION_NOT_FOUND" -> 404;
            case "IDENTITY_FEDERATION_CONNECTION_NOT_ACTIVE" -> 403;
            case "IDENTITY_FEDERATION_PROVIDER_UNAVAILABLE" -> 502;
            case "IDENTITY_OIDC_STATE_INVALID",
                 "IDENTITY_OIDC_NONCE_INVALID" -> 400;
            case "IDENTITY_OIDC_ID_TOKEN_INVALID",
                 "IDENTITY_SAML_RESPONSE_INVALID" -> 401;
            case "IDENTITY_DOMAIN_NOT_VERIFIED" -> 403;
            case "IDENTITY_DOMAIN_CONFLICT" -> 409;
            default -> 500;
        };
        ErrorResponse error = ErrorResponse.of(status, "Federation error", e.getMessage(),
                e.getErrorCode(), requestId != null ? requestId : "");
        return ResponseEntity.status(status).body(error);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
