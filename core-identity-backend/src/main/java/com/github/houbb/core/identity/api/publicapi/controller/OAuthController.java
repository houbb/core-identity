package com.github.houbb.core.identity.api.publicapi.controller;

import com.github.houbb.core.identity.application.domain.RefreshToken;
import com.github.houbb.core.identity.application.domain.RefreshTokenFamily;
import com.github.houbb.core.identity.application.domain.Session;
import com.github.houbb.core.identity.application.port.RefreshTokenFamilyRepository;
import com.github.houbb.core.identity.application.port.RefreshTokenRepository;
import com.github.houbb.core.identity.application.port.SessionRepository;
import com.github.houbb.core.identity.application.port.TokenRevocationRepository;
import com.github.houbb.core.identity.application.service.OAuthAuthorizationService;
import com.github.houbb.core.identity.application.service.OAuthAuthorizationService.*;
import com.github.houbb.core.identity.infrastructure.util.TokenUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.*;

@RestController
public class OAuthController {
    private final OAuthAuthorizationService oauthService;
    private final SessionRepository sessionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenFamilyRepository refreshTokenFamilyRepository;
    private final TokenRevocationRepository tokenRevocationRepository;

    public OAuthController(OAuthAuthorizationService oauthService,
                           SessionRepository sessionRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           RefreshTokenFamilyRepository refreshTokenFamilyRepository,
                           TokenRevocationRepository tokenRevocationRepository) {
        this.oauthService = oauthService;
        this.sessionRepository = sessionRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenFamilyRepository = refreshTokenFamilyRepository;
        this.tokenRevocationRepository = tokenRevocationRepository;
    }

    @GetMapping("/oauth2/authorize")
    public void authorize(@RequestParam String client_id, @RequestParam String redirect_uri,
                          @RequestParam String response_type, @RequestParam(required = false) String scope,
                          @RequestParam(required = false) String state,
                          @RequestParam(required = false) String code_challenge,
                          @RequestParam(required = false) String code_challenge_method,
                          @RequestParam(required = false) String nonce,
                          @RequestParam(required = false) String organization_id,
                          HttpServletRequest request, HttpServletResponse response) throws IOException {
        // A5: Validate required parameters
        if (client_id == null || client_id.isBlank()) {
            response.sendError(400, "client_id is required");
            return;
        }
        if (redirect_uri == null || redirect_uri.isBlank()) {
            response.sendError(400, "redirect_uri is required");
            return;
        }
        if (!"code".equals(response_type)) {
            response.sendError(400, "Unsupported response_type: " + response_type);
            return;
        }

        // A1: Extract user from session cookie properly
        String userId = extractUserIdFromSession(request);
        if (userId == null) {
            response.sendRedirect("/login?redirect=" + java.net.URLEncoder.encode(request.getRequestURI() + "?" + request.getQueryString(), "UTF-8"));
            return;
        }

        // C3: Determine organization_id — prefer request param, fall back to session's lastOrganizationId
        String effectiveOrgId = organization_id;
        if (effectiveOrgId == null || effectiveOrgId.isBlank()) {
            Session session = lookupSessionFromCookie(request);
            if (session != null && session.getLastOrganizationId() != null) {
                effectiveOrgId = session.getLastOrganizationId();
            }
        }

        try {
            AuthorizeResult result = oauthService.authorize(client_id, redirect_uri, response_type,
                    scope != null ? scope : "openid", state, code_challenge, code_challenge_method, nonce, userId, effectiveOrgId);
            response.sendRedirect(result.redirectUrl());
        } catch (Exception e) {
            response.sendError(400, e.getMessage());
        }
    }

    @PostMapping(value = "/oauth2/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> token(@RequestParam String grant_type,
                                   @RequestParam(required = false) String code,
                                   @RequestParam(required = false) String client_id,
                                   @RequestParam(required = false) String client_secret,
                                   @RequestParam(required = false) String redirect_uri,
                                   @RequestParam(required = false) String code_verifier,
                                   @RequestParam(required = false) String refresh_token,
                                   @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // A5: Validate required grant_type
        if (grant_type == null || grant_type.isBlank()) {
            return ResponseEntity.status(400).body(Map.of("error", "invalid_request", "error_description", "grant_type is required"));
        }

        // Support Basic auth for client credentials
        if (client_id == null && authHeader != null && authHeader.startsWith("Basic ")) {
            String[] decoded = new String(Base64.getDecoder().decode(authHeader.substring(6))).split(":", 2);
            client_id = decoded[0];
            client_secret = decoded.length > 1 ? decoded[1] : null;
        }

        // A5: Validate client_id is present
        if (client_id == null || client_id.isBlank()) {
            return ResponseEntity.status(400).body(Map.of("error", "invalid_request", "error_description", "client_id is required"));
        }

        // A5: Validate grant_type specific requirements
        if ("authorization_code".equals(grant_type) && (code == null || code.isBlank())) {
            return ResponseEntity.status(400).body(Map.of("error", "invalid_request", "error_description", "code is required for authorization_code grant"));
        }
        if ("refresh_token".equals(grant_type) && (refresh_token == null || refresh_token.isBlank())) {
            return ResponseEntity.status(400).body(Map.of("error", "invalid_request", "error_description", "refresh_token is required for refresh_token grant"));
        }

        try {
            TokenResponse tr = oauthService.token(grant_type, code, client_id, client_secret, redirect_uri, code_verifier, refresh_token);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("access_token", tr.accessToken());
            resp.put("token_type", tr.tokenType());
            resp.put("expires_in", tr.expiresIn());
            if (tr.refreshToken() != null) resp.put("refresh_token", tr.refreshToken());
            if (tr.idToken() != null) resp.put("id_token", tr.idToken());
            resp.put("scope", tr.scope());
            return ResponseEntity.ok(resp);
        } catch (OAuthAuthorizationService.OAuthException e) {
            return ResponseEntity.status(400).body(Map.of("error", "invalid_grant", "error_description", e.getMessage()));
        }
    }

    @PostMapping("/oauth2/introspect")
    public ResponseEntity<?> introspect(@RequestParam String token) {
        // A5: Validate token is present
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(400).body(Map.of("error", "invalid_request", "error_description", "token is required"));
        }
        IntrospectResponse ir = oauthService.introspect(token);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("active", ir.active());
        if (ir.active()) {
            resp.put("sub", ir.subject()); resp.put("client_id", ir.clientId()); resp.put("scope", ir.scope()); resp.put("exp", ir.exp()); resp.put("iat", ir.iat());
        }
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/oauth2/revoke")
    public ResponseEntity<?> revoke(@RequestParam String token, @RequestParam(required = false) String token_type_hint) {
        // A5: Validate token is present
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(400).body(Map.of("error", "invalid_request", "error_description", "token is required"));
        }

        try {
            // A3: Try to revoke as a refresh token first (by hash lookup)
            String tokenHash = TokenUtils.hashToken(token);
            Optional<RefreshToken> rtOpt = refreshTokenRepository.findByTokenHash(tokenHash);
            if (rtOpt.isPresent()) {
                RefreshToken rt = rtOpt.get();
                if ("ACTIVE".equals(rt.getStatus())) {
                    // Revoke the entire refresh token family
                    revokeEntireTokenFamily(rt.getFamilyId(), "manual_revocation");
                }
                return ResponseEntity.ok(Map.of("status", "revoked"));
            }

            // A3: Not a refresh token — try to parse as JWT and revoke its JTI
            if ("refresh_token".equals(token_type_hint)) {
                // Hint said refresh_token but we didn't find it; token may already be rotated/expired
                return ResponseEntity.ok(Map.of("status", "revoked"));
            }

            try {
                // Parse as JWT to extract JTI and claims, then add to revocation table
                IntrospectResponse ir = oauthService.introspect(token);
                if (ir.active()) {
                    // Extract JTI from the token and add to revocation table
                    String jti = extractJtiFromToken(token);
                    if (jti != null) {
                        tokenRevocationRepository.save(jti, ir.subject(), "manual_revocation", ir.exp());
                    }
                }
            } catch (Exception ignored) {
                // If token can't be parsed or introspected, treat as already revoked/no-op
            }

            return ResponseEntity.ok(Map.of("status", "revoked"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", "invalid_request", "error_description", e.getMessage()));
        }
    }

    @GetMapping("/userinfo")
    public ResponseEntity<?> userinfo(@RequestHeader("Authorization") String authHeader) {
        // A5: Validate Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_token", "error_description", "Missing or invalid Authorization header"));
        }
        try {
            TokenResponse tr = oauthService.validateToken(authHeader.substring(7));
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("sub", "user-from-token"); resp.put("scope", tr.scope());
            resp.put("token_type", "Bearer");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_token", "error_description", e.getMessage()));
        }
    }

    @GetMapping("/api/v1/identity/developer/grants")
    public ResponseEntity<?> listGrants(@RequestParam String userId) {
        // A5: Validate userId is present
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(400).body(Map.of("error", "invalid_request", "error_description", "userId is required"));
        }
        return ResponseEntity.ok(oauthService.getUserGrants(userId));
    }

    @PostMapping("/api/v1/identity/developer/grants/{grantId}/revoke")
    public ResponseEntity<?> revokeGrant(@PathVariable String grantId) {
        // A5: Validate grantId is present
        if (grantId == null || grantId.isBlank()) {
            return ResponseEntity.status(400).body(Map.of("error", "invalid_request", "error_description", "grantId is required"));
        }
        oauthService.revokeGrant(grantId);
        return ResponseEntity.ok(Map.of("status", "revoked"));
    }

    // ============================================================
    // Private helpers
    // ============================================================

    /**
     * A1: Extract userId from session cookie by hashing the cookie value
     * and looking up the session token in the database.
     */
    private String extractUserIdFromSession(HttpServletRequest request) {
        Session session = lookupSessionFromCookie(request);
        return session != null ? session.getUserId() : null;
    }

    /**
     * Look up the Session object from the cookie in the request.
     * Returns null if no valid active session is found.
     */
    private Session lookupSessionFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if ("core_identity_session".equals(c.getName())) {
                String rawToken = c.getValue();
                if (rawToken == null || rawToken.isBlank()) return null;
                String tokenHash = TokenUtils.hashToken(rawToken);
                Optional<Session> sessionOpt = sessionRepository.findByTokenHash(tokenHash);
                if (sessionOpt.isPresent()) {
                    Session session = sessionOpt.get();
                    if ("ACTIVE".equals(session.getStatus())) {
                        return session;
                    }
                }
                return null;
            }
        }
        return null;
    }

    /**
     * Extract JTI from a JWT token without full validation.
     * Used by the revoke endpoint to get the JTI for the revocation table.
     */
    private String extractJtiFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;
            byte[] payloadBytes = java.util.Base64.getUrlDecoder().decode(parts[1]);
            String payloadJson = new String(payloadBytes, java.nio.charset.StandardCharsets.UTF_8);
            // Simple JSON parse for jti
            int jtiIdx = payloadJson.indexOf("\"jti\"");
            if (jtiIdx < 0) return null;
            int colonIdx = payloadJson.indexOf(":", jtiIdx);
            int startQuote = payloadJson.indexOf("\"", colonIdx + 1);
            int endQuote = payloadJson.indexOf("\"", startQuote + 1);
            if (startQuote < 0 || endQuote < 0) return null;
            return payloadJson.substring(startQuote + 1, endQuote);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Revoke an entire refresh token family and all its active tokens.
     */
    private void revokeEntireTokenFamily(String familyId, String reason) {
        RefreshTokenFamily family = refreshTokenFamilyRepository.findById(familyId).orElse(null);
        if (family == null) return;
        long now = System.currentTimeMillis();
        family.setStatus("REVOKED");
        family.setRevokedReason(reason);
        family.setRevokedAt(now);
        refreshTokenFamilyRepository.update(family);

        List<RefreshToken> tokens = refreshTokenRepository.findByFamilyId(familyId);
        for (RefreshToken rt : tokens) {
            if ("ACTIVE".equals(rt.getStatus())) {
                rt.setStatus("REVOKED");
                rt.setUsedAt(now);
                refreshTokenRepository.update(rt);
            }
        }
    }
}