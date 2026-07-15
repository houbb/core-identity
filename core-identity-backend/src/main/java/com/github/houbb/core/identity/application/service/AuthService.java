package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.command.*;

/**
 * Authentication service — registration, login, logout, password management.
 */
public interface AuthService {

    /**
     * Register a new user, create personal organization, send email verification.
     */
    RegistrationResult register(RegistrationCommand command, String clientIp, String userAgent, String requestId);

    /**
     * Confirm email verification with token.
     */
    VerificationResult verifyEmail(String token);

    /**
     * Resend email verification for a pending user.
     */
    void resendEmailVerification(String email);

    /**
     * Login with email and password. Returns session token.
     */
    LoginResult login(LoginCommand command, String clientIp, String userAgent, String requestId);

    /**
     * Logout by revoking the current session.
     */
    void logout(String sessionToken);

    /**
     * Change password for the current user.
     */
    void changePassword(String userId, PasswordChangeCommand command, String requestId);

    /**
     * Request a password reset email.
     */
    void requestPasswordReset(String email);

    /**
     * Complete password reset with token.
     */
    void completePasswordReset(PasswordResetCommand command, String requestId, String clientIp);

    /**
     * Introspect a session token and return the user id if valid.
     */
    String introspectSession(String sessionToken);

    record RegistrationResult(String userId, String organizationId, String emailMasked) {}
    record VerificationResult(boolean success, String message, String userId) {}
    record LoginResult(String userId, String sessionToken, String displayName, String email, String organizationId) {}
}