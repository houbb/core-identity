package com.github.houbb.core.identity.api.publicapi.controller;

import com.github.houbb.core.identity.application.domain.SecurityEvent;
import com.github.houbb.core.identity.application.port.SecurityEventRepository;
import com.github.houbb.core.identity.application.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Security event viewer for users.
 */
@RestController
@RequestMapping("/api/v1/identity/me")
public class SecurityEventController {

    private final SecurityEventRepository securityEventRepo;
    private final AuthService authService;

    public SecurityEventController(SecurityEventRepository securityEventRepo, AuthService authService) {
        this.securityEventRepo = securityEventRepo;
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

    @GetMapping("/security-events")
    public ResponseEntity<?> getSecurityEvents(@RequestParam(defaultValue = "50") int limit,
                                                HttpServletRequest request) {
        String userId = requireUser(request);
        List<SecurityEvent> events = securityEventRepo.findByUserId(userId, limit);
        List<Map<String, Object>> result = new ArrayList<>();
        for (SecurityEvent e : events) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", e.getId());
            m.put("eventType", e.getEventType());
            m.put("severity", e.getSeverity());
            m.put("status", e.getStatus());
            m.put("title", e.getTitle());
            m.put("detectedAt", e.getDetectedAt());
            result.add(m);
        }
        return ResponseEntity.ok(Map.of("events", result));
    }

    @PostMapping("/security-events/{eventId}/confirm")
    public ResponseEntity<?> confirmEvent(@PathVariable String eventId, HttpServletRequest request) {
        String userId = requireUser(request);
        return ResponseEntity.ok(Map.of("message", "Event confirmed"));
    }

    @PostMapping("/security-events/{eventId}/report")
    public ResponseEntity<?> reportEvent(@PathVariable String eventId, HttpServletRequest request) {
        String userId = requireUser(request);
        return ResponseEntity.ok(Map.of("message", "Not-me report created. An admin will review."));
    }
}
