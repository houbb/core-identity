package com.github.houbb.core.identity.api.internal.controller;

import com.github.houbb.core.identity.application.domain.PlatformOperator;
import com.github.houbb.core.identity.application.domain.Session;
import com.github.houbb.core.identity.application.domain.User;
import com.github.houbb.core.identity.application.domain.UserEmail;
import com.github.houbb.core.identity.application.port.*;
import com.github.houbb.core.identity.application.service.AuthService;
import com.github.houbb.core.identity.application.service.AuthServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Internal Admin Auth API: operator login, introspect, logout.
 */
@RestController
@RequestMapping("/internal/v1/identity/admin-auth")
public class InternalAdminAuthController {

    private final UserRepository userRepo;
    private final UserEmailRepository emailRepo;
    private final CredentialRepository credentialRepo;
    private final SessionRepository sessionRepo;
    private final PlatformOperatorRepository operatorRepo;
    private final AuditEventRepository auditRepo;
    private final PasswordHasher passwordHasher;

    public InternalAdminAuthController(UserRepository userRepo,
                                       UserEmailRepository emailRepo,
                                       CredentialRepository credentialRepo,
                                       SessionRepository sessionRepo,
                                       PlatformOperatorRepository operatorRepo,
                                       AuditEventRepository auditRepo,
                                       PasswordHasher passwordHasher) {
        this.userRepo = userRepo;
        this.emailRepo = emailRepo;
        this.credentialRepo = credentialRepo;
        this.sessionRepo = sessionRepo;
        this.operatorRepo = operatorRepo;
        this.auditRepo = auditRepo;
        this.passwordHasher = passwordHasher;
    }

    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "email and password required"));
        }

        String emailNormalized = email.trim().toLowerCase();

        UserEmail userEmail = emailRepo.findByNormalized(emailNormalized).orElse(null);
        if (userEmail == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        // Verify operator
        PlatformOperator operator = operatorRepo.findByUserId(userEmail.getUserId()).orElse(null);
        if (operator == null || !"ACTIVE".equals(operator.getStatus())) {
            return ResponseEntity.status(403).body(Map.of("error", "Platform access denied"));
        }

        // Verify password
        var credential = credentialRepo.findByUserIdAndType(userEmail.getUserId(), "PASSWORD").orElse(null);
        if (credential == null ||
                !passwordHasher.matches(password.toCharArray(), credential.getSecretHash())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        // Check user status
        User user = userRepo.findById(userEmail.getUserId()).orElse(null);
        if (user == null || !"ACTIVE".equals(user.getStatus())) {
            return ResponseEntity.status(403).body(Map.of("error", "Account not active"));
        }

        // Create admin session
        long now = System.currentTimeMillis();
        String rawToken = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        String tokenHash = hashToken(rawToken);

        Session session = new Session();
        session.setId(UUID.randomUUID().toString());
        session.setUserId(user.getId());
        session.setSessionType("ADMIN_WEB");
        session.setTokenHash(tokenHash);
        session.setStatus("ACTIVE");
        session.setLastActiveAt(now);
        session.setIdleExpiresAt(now + 2 * 3600 * 1000L);
        session.setAbsoluteExpiresAt(now + 24 * 3600 * 1000L);
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        session.setVersion(1);
        sessionRepo.save(session);

        Map<String, Object> result = new HashMap<>();
        result.put("sessionToken", rawToken);
        result.put("userId", user.getId());
        result.put("operatorId", operator.getId());
        result.put("operatorRole", operator.getOperatorRole());
        result.put("displayName", user.getDisplayName());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/introspect")
    public ResponseEntity<?> introspect(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token == null) {
            return ResponseEntity.ok(Map.of("active", false));
        }

        String tokenHash = hashToken(token);
        Session session = sessionRepo.findByTokenHash(tokenHash).orElse(null);
        if (session == null || !"ACTIVE".equals(session.getStatus())
                || !"ADMIN_WEB".equals(session.getSessionType())) {
            return ResponseEntity.ok(Map.of("active", false));
        }

        long now = System.currentTimeMillis();
        if (session.getAbsoluteExpiresAt() < now) {
            return ResponseEntity.ok(Map.of("active", false));
        }

        PlatformOperator operator = operatorRepo.findByUserId(session.getUserId()).orElse(null);
        if (operator == null || !"ACTIVE".equals(operator.getStatus())) {
            return ResponseEntity.ok(Map.of("active", false));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("active", true);
        result.put("userId", session.getUserId());
        result.put("operatorId", operator.getId());
        result.put("operatorRole", operator.getOperatorRole());
        result.put("expiresAt", session.getAbsoluteExpiresAt());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> adminLogout(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token != null) {
            String tokenHash = hashToken(token);
            sessionRepo.findByTokenHash(tokenHash).ifPresent(session -> {
                session.setStatus("REVOKED");
                session.setRevokedAt(System.currentTimeMillis());
                session.setRevokeReason("ADMIN_LOGOUT");
                session.setUpdatedAt(System.currentTimeMillis());
                sessionRepo.update(session);
            });
        }
        return ResponseEntity.ok(Map.of("message", "Logged out"));
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