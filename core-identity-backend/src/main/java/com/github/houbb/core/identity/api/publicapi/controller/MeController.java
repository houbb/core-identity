package com.github.houbb.core.identity.api.publicapi.controller;

import com.github.houbb.core.identity.api.response.ErrorResponse;
import com.github.houbb.core.identity.application.command.PasswordChangeCommand;
import com.github.houbb.core.identity.application.domain.Organization;
import com.github.houbb.core.identity.application.domain.Session;
import com.github.houbb.core.identity.application.domain.User;
import com.github.houbb.core.identity.application.domain.UserEmail;
import com.github.houbb.core.identity.application.port.*;
import com.github.houbb.core.identity.application.service.AuthService;
import com.github.houbb.core.identity.application.service.AuthServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Current user APIs — profile, password, sessions.
 */
@RestController
@RequestMapping("/api/v1/identity/me")
public class MeController {

    private final AuthService authService;
    private final UserRepository userRepo;
    private final UserEmailRepository emailRepo;
    private final OrganizationRepository orgRepo;
    private final SessionRepository sessionRepo;

    public MeController(AuthService authService,
                        UserRepository userRepo,
                        UserEmailRepository emailRepo,
                        OrganizationRepository orgRepo,
                        SessionRepository sessionRepo) {
        this.authService = authService;
        this.userRepo = userRepo;
        this.emailRepo = emailRepo;
        this.orgRepo = orgRepo;
        this.sessionRepo = sessionRepo;
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

    private String requireUser(HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        if (userId == null) {
            throw new AuthServiceImpl.AuthException("IDENTITY_SESSION_INVALID", "Not authenticated");
        }
        return userId;
    }

    @GetMapping
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        String userId = requireUser(request);
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new AuthServiceImpl.AuthException("IDENTITY_USER_NOT_FOUND", "User not found"));
        UserEmail email = emailRepo.findByUserId(userId).orElse(null);
        Organization org = orgRepo.findByPersonalOwner(userId).orElse(null);

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("displayName", user.getDisplayName());
        result.put("status", user.getStatus());
        result.put("locale", user.getLocale());
        result.put("timezone", user.getTimezone());

        if (email != null) {
            Map<String, Object> primaryEmail = new HashMap<>();
            primaryEmail.put("address", email.getEmailDisplay());
            primaryEmail.put("verified", email.getVerifiedAt() != null);
            result.put("primaryEmail", primaryEmail);
        }

        if (org != null) {
            Map<String, Object> currentOrg = new HashMap<>();
            currentOrg.put("id", org.getId());
            currentOrg.put("name", org.getName());
            currentOrg.put("type", org.getOrganizationType());
            result.put("currentOrganization", currentOrg);
        }

        result.put("createdAt", user.getCreatedAt());
        return ResponseEntity.ok(result);
    }

    @PatchMapping
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String userId = requireUser(request);
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new AuthServiceImpl.AuthException("IDENTITY_USER_NOT_FOUND", "User not found"));

        if (body.containsKey("displayName")) {
            user.setDisplayName(body.get("displayName"));
        }
        if (body.containsKey("locale")) {
            user.setLocale(body.get("locale"));
        }
        if (body.containsKey("timezone")) {
            user.setTimezone(body.get("timezone"));
        }
        user.setUpdatedAt(System.currentTimeMillis());
        userRepo.update(user);

        return ResponseEntity.ok(Map.of("message", "Profile updated"));
    }

    @PostMapping("/password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody PasswordChangeCommand command,
                                             HttpServletRequest request) {
        String userId = requireUser(request);
        try {
            authService.changePassword(userId, command, request.getHeader("X-Request-ID"));
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (AuthServiceImpl.AuthException e) {
            ErrorResponse error = ErrorResponse.of(400, "Password error", e.getMessage(), e.getErrorCode(), "");
            return ResponseEntity.status(400).body(error);
        }
    }

    @GetMapping("/sessions")
    public ResponseEntity<?> getSessions(HttpServletRequest request) {
        String userId = requireUser(request);
        String currentSessionId = getCurrentSessionId(request);

        long now = System.currentTimeMillis();
        List<Session> sessions = sessionRepo.findActiveByUserId(userId, now);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Session s : sessions) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("deviceName", s.getDeviceName());
            m.put("userAgent", s.getUserAgent());
            m.put("ipAddress", s.getIpAddress());
            m.put("isCurrent", s.getId().equals(currentSessionId));
            m.put("createdAt", s.getCreatedAt());
            m.put("lastActiveAt", s.getLastActiveAt());
            result.add(m);
        }

        return ResponseEntity.ok(Map.of("sessions", result));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<?> revokeSession(@PathVariable String sessionId, HttpServletRequest request) {
        String userId = requireUser(request);
        Session session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new AuthServiceImpl.AuthException("IDENTITY_SESSION_INVALID", "Session not found"));

        if (!session.getUserId().equals(userId)) {
            throw new AuthServiceImpl.AuthException("IDENTITY_SESSION_INVALID", "Cannot revoke another user's session");
        }

        long now = System.currentTimeMillis();
        session.setStatus("REVOKED");
        session.setRevokedAt(now);
        session.setRevokeReason("USER_REVOKED");
        session.setUpdatedAt(now);
        sessionRepo.update(session);

        return ResponseEntity.ok(Map.of("message", "Session revoked"));
    }

    @DeleteMapping("/sessions")
    public ResponseEntity<?> revokeOtherSessions(HttpServletRequest request) {
        String userId = requireUser(request);
        String currentSessionId = getCurrentSessionId(request);
        if (currentSessionId == null) {
            return ResponseEntity.ok(Map.of("message", "No sessions to revoke"));
        }

        long now = System.currentTimeMillis();
        sessionRepo.revokeExceptCurrent(userId, currentSessionId, "USER_REVOKED_ALL", now);

        return ResponseEntity.ok(Map.of("message", "All other sessions revoked"));
    }

    private String getCurrentSessionId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if ("core_identity_session".equals(cookie.getName())) {
                String tokenHash = hashToken(cookie.getValue());
                return sessionRepo.findByTokenHash(tokenHash).map(Session::getId).orElse(null);
            }
        }
        return null;
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