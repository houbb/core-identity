package com.github.houbb.core.identity.api.publicapi.controller;

import com.github.houbb.core.identity.api.response.ErrorResponse;
import com.github.houbb.core.identity.application.command.LoginCommand;
import com.github.houbb.core.identity.application.command.PasswordResetCommand;
import com.github.houbb.core.identity.application.command.RegistrationCommand;
import com.github.houbb.core.identity.application.service.AuthService;
import com.github.houbb.core.identity.application.service.AuthServiceImpl;
import com.github.houbb.core.identity.application.service.AuthServiceImpl.AuthException;
import com.github.houbb.core.identity.application.service.AuthServiceImpl.MustChangePasswordException;
import com.github.houbb.core.identity.application.service.AuthService.LoginResult;
import com.github.houbb.core.identity.application.service.AuthService.RegistrationResult;
import com.github.houbb.core.identity.application.service.AuthService.VerificationResult;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Public auth API: registration, login, logout, password resets, session.
 */
@RestController
@RequestMapping("/api/v1/identity/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ==================== Registration ====================

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationCommand command,
                                       HttpServletRequest request) {
        try {
            RegistrationResult result = authService.register(command,
                    getClientIp(request),
                    request.getHeader("User-Agent"),
                    request.getHeader("X-Request-ID"));
            return ResponseEntity.ok(Map.of(
                    "userId", result.userId(),
                    "organizationId", result.organizationId(),
                    "emailMasked", result.emailMasked(),
                    "message", "Account created. Please check your email for verification."
            ));
        } catch (AuthException e) {
            return error(e, request.getHeader("X-Request-ID"));
        }
    }

    // ==================== Email Verification ====================

    @PostMapping("/email-verifications")
    public ResponseEntity<?> resendVerification(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isEmpty()) {
            return error(new AuthException("IDENTITY_INVALID_REQUEST", "Email is required"), null);
        }
        try {
            authService.resendEmailVerification(email);
            return ResponseEntity.ok(Map.of("message", "If this email is registered, a verification email has been sent."));
        } catch (AuthException e) {
            return error(e, null);
        }
    }

    @PostMapping("/email-verifications/confirm")
    public ResponseEntity<?> confirmVerification(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token == null || token.isEmpty()) {
            return error(new AuthException("IDENTITY_INVALID_REQUEST", "Token is required"), null);
        }
        try {
            VerificationResult result = authService.verifyEmail(token);
            return ResponseEntity.ok(Map.of(
                    "success", result.success(),
                    "message", result.message(),
                    "userId", result.userId()
            ));
        } catch (AuthException e) {
            return error(e, null);
        }
    }

    // ==================== Login ====================

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginCommand command,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        try {
            LoginResult result = authService.login(command,
                    getClientIp(request),
                    request.getHeader("User-Agent"),
                    request.getHeader("X-Request-ID"));

            // Set session cookie
            Cookie cookie = new Cookie("core_identity_session", result.sessionToken());
            cookie.setHttpOnly(true);
            cookie.setSecure(request.isSecure());
            cookie.setPath("/");
            cookie.setMaxAge(24 * 3600); // 24 hours
            cookie.setAttribute("SameSite", "Lax");
            response.addCookie(cookie);

            return ResponseEntity.ok(Map.of(
                    "userId", result.userId(),
                    "displayName", result.displayName(),
                    "email", result.email(),
                    "organizationId", result.organizationId()
            ));
        } catch (AuthException e) {
            return error(e, request.getHeader("X-Request-ID"));
        }
    }

    // ==================== Logout ====================

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("core_identity_session".equals(cookie.getName())) {
                    try {
                        authService.logout(cookie.getValue());
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        // Clear cookie
        Cookie cookie = new Cookie("core_identity_session", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    // ==================== Session Check ====================

    @GetMapping("/session")
    public ResponseEntity<?> checkSession(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return ResponseEntity.status(401).body(Map.of("authenticated", false));
        }

        for (Cookie cookie : cookies) {
            if ("core_identity_session".equals(cookie.getName())) {
                String userId = authService.introspectSession(cookie.getValue());
                if (userId != null) {
                    return ResponseEntity.ok(Map.of("authenticated", true, "userId", userId));
                }
            }
        }
        return ResponseEntity.status(401).body(Map.of("authenticated", false));
    }

    // ==================== Password Reset ====================

    @PostMapping("/password-resets")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isEmpty()) {
            return error(new AuthException("IDENTITY_INVALID_REQUEST", "Email is required"), null);
        }
        authService.requestPasswordReset(email);
        return ResponseEntity.ok(Map.of("message", "If this email is associated with an account, a reset link has been sent."));
    }

    @PostMapping("/password-resets/confirm")
    public ResponseEntity<?> completePasswordReset(@Valid @RequestBody PasswordResetCommand command,
                                                    HttpServletRequest request) {
        try {
            authService.completePasswordReset(command,
                    request.getHeader("X-Request-ID"),
                    getClientIp(request));
            return ResponseEntity.ok(Map.of("message", "Password has been reset. Please log in with your new password."));
        } catch (AuthException e) {
            return error(e, request.getHeader("X-Request-ID"));
        }
    }

    // ==================== Helpers ====================

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private ResponseEntity<ErrorResponse> error(AuthException e, String requestId) {
        int status = switch (e.getErrorCode()) {
            case "IDENTITY_INVALID_CREDENTIALS" -> 401;
            case "IDENTITY_ACCOUNT_LOCKED", "IDENTITY_ACCOUNT_DISABLED" -> 403;
            case "IDENTITY_EMAIL_NOT_VERIFIED" -> 403;
            case "IDENTITY_EMAIL_VERIFICATION_INVALID" -> 400;
            case "IDENTITY_EMAIL_VERIFICATION_EXPIRED" -> 410;
            case "IDENTITY_EMAIL_ALREADY_REGISTERED" -> 409;
            case "IDENTITY_RATE_LIMITED" -> 429;
            case "IDENTITY_PASSWORD_RESET_INVALID" -> 400;
            case "IDENTITY_PASSWORD_RESET_EXPIRED" -> 410;
            case "IDENTITY_IDEMPOTENCY_CONFLICT" -> 409;
            case "IDENTITY_INVALID_REQUEST" -> 400;
            default -> 500;
        };
        ErrorResponse error = ErrorResponse.of(status, "Authentication error", e.getMessage(), e.getErrorCode(),
                requestId != null ? requestId : "");
        return ResponseEntity.status(status).body(error);
    }
}