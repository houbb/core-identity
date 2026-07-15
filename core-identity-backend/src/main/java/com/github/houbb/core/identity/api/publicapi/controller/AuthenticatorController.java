package com.github.houbb.core.identity.api.publicapi.controller;

import com.github.houbb.core.identity.api.response.ErrorResponse;
import com.github.houbb.core.identity.application.domain.Authenticator;
import com.github.houbb.core.identity.application.service.AuthenticatorService;
import com.github.houbb.core.identity.application.service.AuthenticatorServiceImpl;
import com.github.houbb.core.identity.application.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Authenticator management API — list, rename, delete, report compromised.
 */
@RestController
@RequestMapping("/api/v1/identity/me/authenticators")
public class AuthenticatorController {

    private final AuthenticatorService authenticatorService;
    private final AuthService authService;

    public AuthenticatorController(AuthenticatorService authenticatorService, AuthService authService) {
        this.authenticatorService = authenticatorService;
        this.authService = authService;
    }

    private String requireUser(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if ("core_identity_session".equals(cookie.getName())) {
                return authService.introspectSession(cookie.getValue());
            }
        }
        throw new AuthenticatorServiceImpl.AuthenticatorException("IDENTITY_SESSION_INVALID", "Not authenticated");
    }

    @GetMapping
    public ResponseEntity<?> listAuthenticators(HttpServletRequest request) {
        String userId = requireUser(request);
        List<Authenticator> authenticators = authenticatorService.listByUser(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Authenticator a : authenticators) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("type", a.getAuthenticatorType());
            m.put("name", a.getName());
            m.put("status", a.getStatus());
            m.put("assuranceLevel", a.getAssuranceLevel());
            m.put("phishingResistant", a.getPhishingResistant() == 1);
            m.put("enrolledAt", a.getEnrolledAt());
            m.put("lastUsedAt", a.getLastUsedAt());
            result.add(m);
        }
        return ResponseEntity.ok(Map.of("authenticators", result));
    }

    @DeleteMapping("/{authenticatorId}")
    public ResponseEntity<?> deleteAuthenticator(@PathVariable String authenticatorId,
                                                  HttpServletRequest request) {
        String userId = requireUser(request);
        try {
            // Check authenticator belongs to user
            List<Authenticator> all = authenticatorService.listByUser(userId);
            boolean found = all.stream().anyMatch(a -> a.getId().equals(authenticatorId));
            if (!found) {
                return ResponseEntity.status(404).body(Map.of("error", "Authenticator not found"));
            }
            authenticatorService.revoke(authenticatorId);
            return ResponseEntity.ok(Map.of("message", "Authenticator removed"));
        } catch (AuthenticatorServiceImpl.AuthenticatorException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{authenticatorId}/rename")
    public ResponseEntity<?> renameAuthenticator(@PathVariable String authenticatorId,
                                                  @RequestBody Map<String, String> body,
                                                  HttpServletRequest request) {
        String userId = requireUser(request);
        String newName = body.get("name");
        if (newName == null || newName.isEmpty()) {
            return ResponseEntity.status(400).body(Map.of("error", "Name is required"));
        }
        try {
            authenticatorService.rename(authenticatorId, newName);
            return ResponseEntity.ok(Map.of("message", "Renamed"));
        } catch (AuthenticatorServiceImpl.AuthenticatorException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{authenticatorId}/report-compromised")
    public ResponseEntity<?> reportCompromised(@PathVariable String authenticatorId,
                                                HttpServletRequest request) {
        String userId = requireUser(request);
        try {
            authenticatorService.markCompromised(authenticatorId);
            return ResponseEntity.ok(Map.of("message", "Authenticator marked as compromised"));
        } catch (AuthenticatorServiceImpl.AuthenticatorException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }
}
