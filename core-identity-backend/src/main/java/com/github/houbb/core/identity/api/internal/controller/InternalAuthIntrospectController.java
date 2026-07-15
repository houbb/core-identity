package com.github.houbb.core.identity.api.internal.controller;

import com.github.houbb.core.identity.application.service.ApiKeyService;
import com.github.houbb.core.identity.application.service.OAuthTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Internal API for API Key and Token introspection.
 * Used by Gateway and other Core services for authentication verification.
 */
@RestController
@RequestMapping("/internal/v1/identity")
public class InternalAuthIntrospectController {

    private final ApiKeyService apiKeyService;
    private final OAuthTokenService tokenService;

    public InternalAuthIntrospectController(ApiKeyService apiKeyService, OAuthTokenService tokenService) {
        this.apiKeyService = apiKeyService;
        this.tokenService = tokenService;
    }

    /**
     * Introspect an API Key — verify it and return owner information.
     * Used by Gateway instead of issuing a full JWT for every API key call.
     */
    @PostMapping("/api-keys/introspect")
    public ResponseEntity<Map<String, Object>> introspectApiKey(@RequestBody Map<String, Object> body) {
        String rawKey = (String) body.get("apiKey");
        if (rawKey == null || rawKey.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid_request", "error_description", "apiKey is required"));
        }

        var result = apiKeyService.introspect(rawKey);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("active", result.active());
        if (result.active()) {
            resp.put("ownerType", result.ownerType());
            resp.put("ownerId", result.ownerId());
            resp.put("organizationId", result.organizationId());
            resp.put("keyPrefix", result.keyPrefix());
        }
        return ResponseEntity.ok(resp);
    }

    /**
     * Introspect a JWT Access Token — verify and return claims.
     * Used by resource servers that cannot locally verify JWT signatures.
     */
    @PostMapping("/tokens/introspect")
    public ResponseEntity<Map<String, Object>> introspectToken(@RequestBody Map<String, Object> body) {
        String token = (String) body.get("token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid_request", "error_description", "token is required"));
        }

        try {
            var claims = tokenService.validateToken(token);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("active", true);
            resp.put("sub", claims.subject());
            resp.put("subjectType", claims.subjectType());
            resp.put("clientId", claims.clientId());
            resp.put("organizationId", claims.organizationId());
            resp.put("scope", claims.scope());
            resp.put("jti", claims.jti());
            resp.put("iat", claims.iat());
            resp.put("exp", claims.exp());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("active", false));
        }
    }
}