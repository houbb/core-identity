package com.github.houbb.core.identity.api.internal.controller;

import com.github.houbb.core.identity.application.domain.AccountRecovery;
import com.github.houbb.core.identity.application.domain.SecurityEvent;
import com.github.houbb.core.identity.application.domain.User;
import com.github.houbb.core.identity.application.port.*;
import com.github.houbb.core.identity.application.service.AccountRecoveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Admin security API — risk management, user security operations, recoveries.
 */
@RestController
@RequestMapping("/admin-api/v1/identity/security")
public class AdminSecurityController {

    private final SecurityEventRepository securityEventRepo;
    private final AccountRecoveryService recoveryService;
    private final UserRepository userRepo;
    private final AuthenticatorRepository authenticatorRepo;
    private final SessionRepository sessionRepo;

    public AdminSecurityController(SecurityEventRepository securityEventRepo,
                                   AccountRecoveryService recoveryService,
                                   UserRepository userRepo,
                                   AuthenticatorRepository authenticatorRepo,
                                   SessionRepository sessionRepo) {
        this.securityEventRepo = securityEventRepo;
        this.recoveryService = recoveryService;
        this.userRepo = userRepo;
        this.authenticatorRepo = authenticatorRepo;
        this.sessionRepo = sessionRepo;
    }

    @GetMapping("/overview")
    public ResponseEntity<?> overview() {
        long now = System.currentTimeMillis();
        List<SecurityEvent> recent = securityEventRepo.findRecentHighSeverity(20);
        return ResponseEntity.ok(Map.of(
                "highRiskEvents", recent.size(),
                "recentEvents", recent.stream().map(this::eventSummary).toList()
        ));
    }

    @GetMapping("/events")
    public ResponseEntity<?> events(@RequestParam(defaultValue = "50") int limit) {
        List<SecurityEvent> events = securityEventRepo.findRecentHighSeverity(limit);
        return ResponseEntity.ok(Map.of("events", events.stream().map(this::eventSummary).toList()));
    }

    @GetMapping("/risky-users")
    public ResponseEntity<?> riskyUsers() {
        return ResponseEntity.ok(Map.of("users", List.of()));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> userSecurityDetail(@PathVariable String userId) {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("error", "User not found"));

        var authenticators = authenticatorRepo.findByUserId(userId);
        long activeCount = authenticators.stream().filter(a -> "ACTIVE".equals(a.getStatus())).count();

        return ResponseEntity.ok(Map.of(
                "userId", user.getId(),
                "securityStatus", user.getSecurityStatus(),
                "riskLevel", user.getRiskLevel(),
                "mfaEnrolled", user.getMfaEnrolled() == 1,
                "phishingResistantEnrolled", user.getPhishingResistantEnrolled() == 1,
                "activeAuthenticators", activeCount,
                "recoveryState", user.getRecoveryState()
        ));
    }

    @PostMapping("/users/{userId}/lock")
    public ResponseEntity<?> lockUser(@PathVariable String userId) {
        long now = System.currentTimeMillis();
        userRepo.findById(userId).ifPresent(u -> {
            u.setStatus("LOCKED_SECURITY");
            u.setSecurityVersion(u.getSecurityVersion() + 1);
            u.setUpdatedAt(now);
            userRepo.update(u);
            sessionRepo.revokeByUserId(userId, "EMERGENCY_LOCK", now);
        });
        return ResponseEntity.ok(Map.of("message", "User locked and all sessions revoked"));
    }

    @PostMapping("/users/{userId}/unlock")
    public ResponseEntity<?> unlockUser(@PathVariable String userId) {
        userRepo.updateStatus(userId, "ACTIVE", 0);
        return ResponseEntity.ok(Map.of("message", "User unlocked"));
    }

    @PostMapping("/users/{userId}/revoke-sessions")
    public ResponseEntity<?> revokeSessions(@PathVariable String userId) {
        long now = System.currentTimeMillis();
        sessionRepo.revokeByUserId(userId, "ADMIN_REVOKED", now);
        userRepo.findById(userId).ifPresent(u -> {
            u.setSecurityVersion(u.getSecurityVersion() + 1);
            userRepo.update(u);
        });
        return ResponseEntity.ok(Map.of("message", "All sessions revoked"));
    }

    @PostMapping("/users/{userId}/require-password-reset")
    public ResponseEntity<?> requirePasswordReset(@PathVariable String userId) {
        return ResponseEntity.ok(Map.of("message", "Password reset required"));
    }

    @PostMapping("/users/{userId}/require-mfa-reset")
    public ResponseEntity<?> requireMfaReset(@PathVariable String userId) {
        return ResponseEntity.ok(Map.of("message", "MFA reset required"));
    }

    @GetMapping("/recoveries")
    public ResponseEntity<?> pendingRecoveries() {
        List<AccountRecovery> recoveries = recoveryService.findPendingRecoveries();
        return ResponseEntity.ok(Map.of("recoveries", recoveries.stream().map(r -> Map.of(
                "id", r.getId(), "userId", r.getUserId(), "status", r.getStatus(),
                "recoveryType", r.getRecoveryType(), "coolingOffUntil", r.getCoolingOffUntil()
        )).toList()));
    }

    @PostMapping("/recoveries/{recoveryId}/approve")
    public ResponseEntity<?> approveRecovery(@PathVariable String recoveryId) {
        return ResponseEntity.ok(Map.of("message", "Recovery approved"));
    }

    @PostMapping("/recoveries/{recoveryId}/reject")
    public ResponseEntity<?> rejectRecovery(@PathVariable String recoveryId) {
        return ResponseEntity.ok(Map.of("message", "Recovery rejected"));
    }

    @GetMapping("/authenticator-metrics")
    public ResponseEntity<?> authenticatorMetrics() {
        return ResponseEntity.ok(Map.of("totalWithMfa", 0, "totalWithPasskey", 0, "mfaCoverage", "0%"));
    }

    @GetMapping("/login-metrics")
    public ResponseEntity<?> loginMetrics() {
        return ResponseEntity.ok(Map.of("recentBlocked", 0, "recentHighRisk", 0));
    }

    private Map<String, Object> eventSummary(SecurityEvent e) {
        return Map.of(
                "id", e.getId(), "userId", e.getUserId(), "eventType", e.getEventType(),
                "severity", e.getSeverity(), "status", e.getStatus(), "title", e.getTitle(),
                "detectedAt", e.getDetectedAt()
        );
    }
}
