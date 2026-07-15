package com.github.houbb.core.identity.application.port;

/**
 * Notification port — decouples identity from concrete notification delivery.
 */
public interface IdentityNotificationPort {

    void sendEmailVerification(String email, String displayName, String token, String verificationUrl);

    void sendPasswordReset(String email, String displayName, String token, String resetUrl);

    void sendPasswordChanged(String email, String displayName);

    void sendAccountDisabled(String email, String displayName, String reason);

    void sendAccountCreated(String email, String displayName, String setupUrl);
}