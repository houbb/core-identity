package com.github.houbb.core.identity.api.publicapi.controller;

import com.github.houbb.core.identity.application.service.AccountRecoveryService;
import com.github.houbb.core.identity.application.service.AuthService;
import com.github.houbb.core.identity.application.domain.AccountRecovery;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Account recovery controller.
 */
@RestController
@RequestMapping("/api/v1/identity/account-recoveries")
public class AccountRecoveryController {

    private final AccountRecoveryService recoveryService;
    private final AuthService authService;

    public AccountRecoveryController(AccountRecoveryService recoveryService, AuthService authService) {
        this.recoveryService = recoveryService;
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

    @PostMapping
    public ResponseEntity<?> initiate(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String userId = body.get("userId");
        String recoveryType = body.getOrDefault("recoveryType", "PASSWORD_RESET");
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) ip = request.getRemoteAddr();

        if (userId == null) {
            return ResponseEntity.status(400).body(Map.of("error", "userId is required"));
        }

        try {
            AccountRecovery recovery = recoveryService.initiate(userId, recoveryType, ip, null);
            return ResponseEntity.ok(Map.of(
                    "recoveryId", recovery.getId(),
                    "status", recovery.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{recoveryId}")
    public ResponseEntity<?> getStatus(@PathVariable String recoveryId) {
        return recoveryService.findById(recoveryId)
                .map(r -> ResponseEntity.ok(Map.of(
                        "id", r.getId(),
                        "userId", r.getUserId(),
                        "status", r.getStatus(),
                        "recoveryType", r.getRecoveryType(),
                        "coolingOffUntil", r.getCoolingOffUntil()
                )))
                .orElse(ResponseEntity.status(404).body(Map.of("error", "Recovery not found")));
    }

    @PostMapping("/{recoveryId}/verify")
    public ResponseEntity<?> verify(@PathVariable String recoveryId, HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        try {
            recoveryService.verify(recoveryId, userId);
            return ResponseEntity.ok(Map.of("message", "Verification accepted. Recovery is in cooling-off period."));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{recoveryId}/cancel")
    public ResponseEntity<?> cancel(@PathVariable String recoveryId, HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        try {
            recoveryService.cancel(recoveryId, userId);
            return ResponseEntity.ok(Map.of("message", "Recovery cancelled"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{recoveryId}/complete")
    public ResponseEntity<?> complete(@PathVariable String recoveryId, HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        try {
            recoveryService.complete(recoveryId, userId);
            return ResponseEntity.ok(Map.of("message", "Account recovery completed. Please set a new password and reconfigure your authenticators."));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }
}
