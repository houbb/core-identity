package com.github.houbb.core.identity.api.publicapi.controller;

import com.github.houbb.core.identity.application.service.AuthenticatorService;
import com.github.houbb.core.identity.application.service.AuthenticatorServiceImpl;
import com.github.houbb.core.identity.application.service.AuthService;
import com.github.houbb.core.identity.application.service.TotpService;
import com.github.houbb.core.identity.application.service.TotpServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * TOTP enrollment and verification controller.
 */
@RestController
@RequestMapping("/api/v1/identity/me/authenticators/totp")
public class TotpController {

    private final TotpService totpService;
    private final AuthService authService;

    public TotpController(TotpService totpService, AuthService authService) {
        this.totpService = totpService;
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

    @PostMapping("/enroll")
    public ResponseEntity<?> enroll(HttpServletRequest request) {
        String userId = requireUser(request);
        try {
            // Use email as account name if available
            TotpService.TotpEnrollmentResult result = totpService.enroll(userId, "CoreIdentity", userId);
            return ResponseEntity.ok(Map.of(
                    "authenticatorId", result.authenticatorId(),
                    "qrCodeUri", result.qrCodeUri(),
                    "manualKey", result.manualKey()
            ));
        } catch (AuthenticatorServiceImpl.AuthenticatorException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody Map<String, String> body,
                                      HttpServletRequest request) {
        String userId = requireUser(request);
        String authenticatorId = body.get("authenticatorId");
        String code = body.get("code");

        if (authenticatorId == null || code == null) {
            return ResponseEntity.status(400).body(Map.of("error", "authenticatorId and code are required"));
        }

        try {
            totpService.confirm(userId, authenticatorId, code);
            return ResponseEntity.ok(Map.of("message", "TOTP authenticator activated"));
        } catch (TotpServiceImpl.TotpException | AuthenticatorServiceImpl.AuthenticatorException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancel(@RequestBody Map<String, String> body,
                                     HttpServletRequest request) {
        String userId = requireUser(request);
        String authenticatorId = body.get("authenticatorId");
        if (authenticatorId == null) {
            return ResponseEntity.status(400).body(Map.of("error", "authenticatorId is required"));
        }

        try {
            totpService.cancelEnrollment(authenticatorId);
            return ResponseEntity.ok(Map.of("message", "TOTP enrollment cancelled"));
        } catch (AuthenticatorServiceImpl.AuthenticatorException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }
}
