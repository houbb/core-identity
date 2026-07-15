package com.github.houbb.core.identity.api.publicapi.controller;

import com.github.houbb.core.identity.application.service.AuthService;
import com.github.houbb.core.identity.application.service.WebAuthnService;
import com.github.houbb.core.identity.application.service.WebAuthnServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * WebAuthn / Passkey registration and authentication controller.
 */
@RestController
@RequestMapping("/api/v1/identity/webauthn")
public class WebAuthnController {

    private final WebAuthnService webAuthnService;
    private final AuthService authService;

    public WebAuthnController(WebAuthnService webAuthnService, AuthService authService) {
        this.webAuthnService = webAuthnService;
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

    // === Registration ===

    @PostMapping("/registration/options")
    public ResponseEntity<?> registrationOptions(HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        try {
            WebAuthnService.WebAuthnRegistrationOptions options = webAuthnService.generateRegistrationOptions(userId, userId);
            return ResponseEntity.ok(Map.of(
                    "challenge", options.challenge(),
                    "rp", Map.of("id", options.rpId(), "name", options.rpName()),
                    "user", Map.of("id", options.userId(), "name", options.userName(), "displayName", options.userDisplayName()),
                    "timeout", options.timeout(),
                    "authenticatorId", options.userId() // placeholder — actual ID from PENDING authenticator
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/registration/verify")
    public ResponseEntity<?> registrationVerify(@RequestBody Map<String, Object> body,
                                                 HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        String authenticatorId = (String) body.get("authenticatorId");
        if (authenticatorId == null) {
            return ResponseEntity.status(400).body(Map.of("error", "authenticatorId is required"));
        }
        try {
            webAuthnService.verifyRegistration(userId, authenticatorId, body);
            return ResponseEntity.ok(Map.of("message", "Passkey registered successfully"));
        } catch (WebAuthnServiceImpl.WebAuthnException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    // === Authentication ===

    @PostMapping("/authentication/options")
    public ResponseEntity<?> authenticationOptions(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        try {
            WebAuthnService.WebAuthnAuthenticationOptions options = webAuthnService.generateAuthenticationOptions(email);
            return ResponseEntity.ok(Map.of(
                    "challenge", options.challenge(),
                    "rpId", options.rpId(),
                    "allowCredentials", options.allowCredentialsJson(),
                    "timeout", options.timeout()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/authentication/verify")
    public ResponseEntity<?> authenticationVerify(@RequestBody Map<String, Object> body,
                                                   HttpServletRequest request,
                                                   HttpServletResponse response) {
        try {
            String userId = webAuthnService.verifyAuthentication(body);

            // Create session (delegated to auth service pattern)
            // For now, return userId — frontend completes login
            return ResponseEntity.ok(Map.of(
                    "userId", userId,
                    "authenticated", true
            ));
        } catch (WebAuthnServiceImpl.WebAuthnException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }
}
