package com.github.houbb.core.identity.infrastructure.notification;

import com.github.houbb.core.identity.application.port.IdentityNotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Console notification adapter — prints verification URLs to console for development.
 * NOT for production use.
 */
@Component
@Profile({"default", "dev", "h2", "sqlite"})
public class ConsoleNotificationAdapter implements IdentityNotificationPort {

    private static final Logger log = LoggerFactory.getLogger(ConsoleNotificationAdapter.class);

    @Override
    public void sendEmailVerification(String email, String displayName, String token, String verificationUrl) {
        log.info("======================================");
        log.info("EMAIL VERIFICATION for: {}", email);
        log.info("User: {}", displayName);
        log.info("Token: {}", token);
        log.info("URL: {}", verificationUrl);
        log.info("======================================");
    }

    @Override
    public void sendPasswordReset(String email, String displayName, String token, String resetUrl) {
        log.info("======================================");
        log.info("PASSWORD RESET for: {}", email);
        log.info("User: {}", displayName);
        log.info("Token: {}", token);
        log.info("URL: {}", resetUrl);
        log.info("======================================");
    }

    @Override
    public void sendPasswordChanged(String email, String displayName) {
        log.info("PASSWORD CHANGED for: {} ({})", email, displayName);
    }

    @Override
    public void sendAccountDisabled(String email, String displayName, String reason) {
        log.info("ACCOUNT DISABLED: {} ({}) reason: {}", email, displayName, reason);
    }

    @Override
    public void sendAccountCreated(String email, String displayName, String setupUrl) {
        log.info("======================================");
        log.info("ACCOUNT CREATED by admin for: {}", email);
        log.info("User: {}", displayName);
        log.info("Setup URL: {}", setupUrl);
        log.info("======================================");
    }
}