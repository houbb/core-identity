package com.github.houbb.core.identity.api.publicapi.controller;

import com.github.houbb.core.identity.application.service.AuthService;
import com.github.houbb.core.identity.application.service.RecoveryCodeService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Recovery code management controller.
 */
@RestController
@RequestMapping("/api/v1/identity/me/recovery-codes")
public class RecoveryCodeController {

    private final RecoveryCodeService recoveryCodeService;
    private final AuthService authService;

    public RecoveryCodeController(RecoveryCodeService recoveryCodeService, AuthService authService) {
        this.recoveryCodeService = recoveryCodeService;
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
        throw new RuntimeException("Not authenticated");
    }

    @PostMapping
    public ResponseEntity<?> generate(HttpServletRequest request) {
        String userId = requireUser(request);
        List<String> codes = recoveryCodeService.generate(userId);
        return ResponseEntity.ok(Map.of("recoveryCodes", codes, "message", "Save these codes in a secure location"));
    }

    @GetMapping("/status")
    public ResponseEntity<?> status(HttpServletRequest request) {
        String userId = requireUser(request);
        RecoveryCodeService.RecoveryCodeStatus status = recoveryCodeService.getStatus(userId);
        return ResponseEntity.ok(Map.of(
                "remainingCount", status.remainingCount(),
                "totalCount", status.totalCount(),
                "generatedAt", status.generatedAt(),
                "isActive", status.isActive()
        ));
    }
}