package com.github.houbb.core.identity.admin.api.controller;

import com.github.houbb.core.identity.admin.infrastructure.identityclient.IdentityInternalClient;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin Auth Controller — proxies login to Identity Backend.
 */
@RestController
@RequestMapping("/admin-api/v1/identity/auth")
public class AdminAuthController {

    private final IdentityInternalClient identityClient;

    public AdminAuthController(IdentityInternalClient identityClient) {
        this.identityClient = identityClient;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpServletResponse response) {
        String email = body.get("email");
        String password = body.get("password");

        try {
            Map<String, Object> result = identityClient.adminLogin(email, password);

            // Set admin session cookie
            String sessionToken = (String) result.get("sessionToken");
            Cookie cookie = new Cookie("core_identity_admin_session", sessionToken);
            cookie.setHttpOnly(true);
            cookie.setSecure(false); // dev
            cookie.setPath("/");
            cookie.setMaxAge(24 * 3600);
            cookie.setAttribute("SameSite", "Lax");
            response.addCookie(cookie);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("core_identity_admin_session".equals(cookie.getName())) {
                    try { identityClient.adminLogout(cookie.getValue()); } catch (Exception ignored) {}
                }
            }
        }
        Cookie cookie = new Cookie("core_identity_admin_session", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        for (Cookie cookie : cookies) {
            if ("core_identity_admin_session".equals(cookie.getName())) {
                try {
                    Map<String, Object> result = identityClient.adminIntrospect(cookie.getValue());
                    if (Boolean.TRUE.equals(result.get("active"))) {
                        return ResponseEntity.ok(result);
                    }
                } catch (Exception ignored) {}
            }
        }
        return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
    }
}