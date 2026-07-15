package com.github.houbb.core.identity.api.internal.controller;

import com.github.houbb.core.identity.application.domain.*;
import com.github.houbb.core.identity.application.port.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

/**
 * Internal User Management API — for Admin Backend BFF.
 */
@RestController
@RequestMapping("/internal/v1/identity/users")
public class InternalUserManagementController {

    private final UserRepository userRepo;
    private final UserEmailRepository emailRepo;
    private final CredentialRepository credentialRepo;
    private final OrganizationRepository orgRepo;
    private final MembershipRepository membershipRepo;
    private final SessionRepository sessionRepo;
    private final OneTimeTokenRepository tokenRepo;
    private final LoginAttemptRepository loginAttemptRepo;
    private final PlatformOperatorRepository operatorRepo;

    public InternalUserManagementController(UserRepository userRepo,
                                            UserEmailRepository emailRepo,
                                            CredentialRepository credentialRepo,
                                            OrganizationRepository orgRepo,
                                            MembershipRepository membershipRepo,
                                            SessionRepository sessionRepo,
                                            OneTimeTokenRepository tokenRepo,
                                            LoginAttemptRepository loginAttemptRepo,
                                            PlatformOperatorRepository operatorRepo) {
        this.userRepo = userRepo;
        this.emailRepo = emailRepo;
        this.credentialRepo = credentialRepo;
        this.orgRepo = orgRepo;
        this.membershipRepo = membershipRepo;
        this.sessionRepo = sessionRepo;
        this.tokenRepo = tokenRepo;
        this.loginAttemptRepo = loginAttemptRepo;
        this.operatorRepo = operatorRepo;
    }

    @GetMapping
    public ResponseEntity<?> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String email) {

        // Simple approach: use raw JDBC for listing with filters
        // In a real implementation this would be more sophisticated
        List<Map<String, Object>> results = new ArrayList<>();

        // Stub: return empty for now until we add proper search support
        return ResponseEntity.ok(Map.of("users", results, "total", 0, "page", page, "size", size));
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String displayName = body.get("displayName");
        String operatorId = body.get("operatorId");
        String reason = body.get("reason");

        if (email == null || displayName == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "email and displayName required"));
        }

        String emailNormalized = email.trim().toLowerCase();

        // Check existing
        if (emailRepo.findByNormalized(emailNormalized).isPresent()) {
            return ResponseEntity.status(409).body(Map.of("error", "Email already registered"));
        }

        long now = Instant.now().toEpochMilli();

        // Create pending user
        String userId = UUID.randomUUID().toString();
        User user = new User();
        user.setId(userId);
        user.setDisplayName(displayName);
        user.setStatus("PENDING_VERIFICATION");
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setVersion(1);
        userRepo.save(user);

        UserEmail userEmail = new UserEmail();
        userEmail.setId(UUID.randomUUID().toString());
        userEmail.setUserId(userId);
        userEmail.setEmailNormalized(emailNormalized);
        userEmail.setEmailDisplay(email);
        userEmail.setIsPrimary(1);
        userEmail.setCreatedAt(now);
        userEmail.setUpdatedAt(now);
        userEmail.setVersion(1);
        emailRepo.save(userEmail);

        // Personal org
        String orgId = UUID.randomUUID().toString();
        Organization org = new Organization();
        org.setId(orgId);
        org.setOrganizationType("PERSONAL");
        org.setName(displayName + "'s Workspace");
        org.setPersonalOwnerUserId(userId);
        org.setStatus("ACTIVE");
        org.setCreatedAt(now);
        org.setUpdatedAt(now);
        org.setVersion(1);
        orgRepo.save(org);

        Membership membership = new Membership();
        membership.setId(UUID.randomUUID().toString());
        membership.setOrganizationId(orgId);
        membership.setUserId(userId);
        membership.setMembershipType("OWNER");
        membership.setStatus("ACTIVE");
        membership.setJoinedAt(now);
        membership.setCreatedAt(now);
        membership.setUpdatedAt(now);
        membership.setVersion(1);
        membershipRepo.save(membership);

        return ResponseEntity.ok(Map.of("userId", userId));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserDetail(@PathVariable String userId) {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        UserEmail email = emailRepo.findByUserId(userId).orElse(null);
        Organization org = orgRepo.findByPersonalOwner(userId).orElse(null);

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("displayName", user.getDisplayName());
        result.put("status", user.getStatus());
        result.put("locale", user.getLocale());
        result.put("timezone", user.getTimezone());
        result.put("disabledAt", user.getDisabledAt());
        result.put("disabledReason", user.getDisabledReason());
        result.put("lockedUntil", user.getLockedUntil());
        result.put("lastLoginAt", user.getLastLoginAt());
        result.put("createdAt", user.getCreatedAt());

        if (email != null) {
            result.put("email", email.getEmailDisplay());
            result.put("emailVerified", email.getVerifiedAt() != null);
        }

        if (org != null) {
            result.put("personalOrganizationId", org.getId());
            result.put("personalOrganizationName", org.getName());
        }

        List<Session> sessions = sessionRepo.findByUserIdAndStatus(userId, "ACTIVE");
        result.put("activeSessions", sessions.size());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/{userId}/disable")
    public ResponseEntity<?> disableUser(@PathVariable String userId, @RequestBody Map<String, String> body) {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        long now = Instant.now().toEpochMilli();
        String reason = body.getOrDefault("reason", "Admin action");

        user.setStatus("DISABLED");
        user.setDisabledAt(now);
        user.setDisabledReason(reason);
        user.setUpdatedAt(now);
        userRepo.update(user);

        sessionRepo.revokeByUserId(userId, "ADMIN_DISABLE", now);
        tokenRepo.revokeAllForUser(userId);

        return ResponseEntity.ok(Map.of("message", "User disabled"));
    }

    @PostMapping("/{userId}/enable")
    public ResponseEntity<?> enableUser(@PathVariable String userId) {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        long now = Instant.now().toEpochMilli();
        UserEmail email = emailRepo.findByUserId(userId).orElse(null);
        String newStatus = (email != null && email.getVerifiedAt() != null) ? "ACTIVE" : "PENDING_VERIFICATION";

        user.setStatus(newStatus);
        user.setDisabledAt(null);
        user.setDisabledReason(null);
        user.setUpdatedAt(now);
        userRepo.update(user);

        return ResponseEntity.ok(Map.of("message", "User enabled"));
    }

    @PostMapping("/{userId}/revoke-sessions")
    public ResponseEntity<?> revokeSessions(@PathVariable String userId) {
        long now = Instant.now().toEpochMilli();
        sessionRepo.revokeByUserId(userId, "ADMIN_REVOKE", now);
        return ResponseEntity.ok(Map.of("message", "Sessions revoked"));
    }

    @PostMapping("/{userId}/resend-verification")
    public ResponseEntity<?> resendVerification(@PathVariable String userId) {
        UserEmail email = emailRepo.findByUserId(userId).orElse(null);
        if (email == null) return ResponseEntity.notFound().build();

        tokenRepo.revokeAllForUserAndType(userId, "EMAIL_VERIFICATION");

        long now = Instant.now().toEpochMilli();
        String rawToken = UUID.randomUUID().toString();
        OneTimeToken token = new OneTimeToken();
        token.setId(UUID.randomUUID().toString());
        token.setUserId(userId);
        token.setTokenType("EMAIL_VERIFICATION");
        token.setTokenHash(hashToken(rawToken));
        token.setStatus("ACTIVE");
        token.setExpiresAt(now + 30 * 60 * 1000L);
        token.setCreatedAt(now);
        token.setUpdatedAt(now);
        token.setVersion(1);
        tokenRepo.save(token);

        return ResponseEntity.ok(Map.of("message", "Verification email queued", "token", rawToken));
    }

    @PostMapping("/{userId}/send-password-reset")
    public ResponseEntity<?> sendPasswordReset(@PathVariable String userId) {
        UserEmail email = emailRepo.findByUserId(userId).orElse(null);
        if (email == null) return ResponseEntity.notFound().build();

        tokenRepo.revokeAllForUserAndType(userId, "PASSWORD_RESET");

        long now = Instant.now().toEpochMilli();
        String rawToken = UUID.randomUUID().toString();
        OneTimeToken token = new OneTimeToken();
        token.setId(UUID.randomUUID().toString());
        token.setUserId(userId);
        token.setTokenType("PASSWORD_RESET");
        token.setTokenHash(hashToken(rawToken));
        token.setStatus("ACTIVE");
        token.setExpiresAt(now + 30 * 60 * 1000L);
        token.setCreatedAt(now);
        token.setUpdatedAt(now);
        token.setVersion(1);
        tokenRepo.save(token);

        return ResponseEntity.ok(Map.of("message", "Password reset email queued", "token", rawToken));
    }

    @GetMapping("/{userId}/login-attempts")
    public ResponseEntity<?> getLoginAttempts(@PathVariable String userId) {
        List<LoginAttempt> attempts = loginAttemptRepo.findByUserId(userId, 50);
        return ResponseEntity.ok(Map.of("attempts", attempts));
    }

    private String hashToken(String rawToken) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(rawToken.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return rawToken;
        }
    }
}