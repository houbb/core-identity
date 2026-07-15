package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.command.*;
import com.github.houbb.core.identity.application.domain.*;
import com.github.houbb.core.identity.application.port.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;	

/**
 * Default implementation of AuthService.
 */
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private static final int SESSION_IDLE_HOURS = 2;
    private static final int SESSION_ABSOLUTE_HOURS = 24;
    private static final int TOKEN_EXPIRE_MINUTES = 30;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MS = 15 * 60 * 1000L;

    private final UserRepository userRepo;
    private final UserEmailRepository emailRepo;
    private final CredentialRepository credentialRepo;
    private final OrganizationRepository orgRepo;
    private final MembershipRepository membershipRepo;
    private final SessionRepository sessionRepo;
    private final OneTimeTokenRepository tokenRepo;
    private final LoginAttemptRepository loginAttemptRepo;
    private final PlatformOperatorRepository operatorRepo;
    private final AuditService auditService;
    private final OutboxService outboxService;
    private final IdempotencyService idempotencyService;
    private final PasswordHasher passwordHasher;
    private final IdentityNotificationPort notificationPort;

    private final SecureRandom secureRandom = new SecureRandom();

    public AuthServiceImpl(UserRepository userRepo,
                           UserEmailRepository emailRepo,
                           CredentialRepository credentialRepo,
                           OrganizationRepository orgRepo,
                           MembershipRepository membershipRepo,
                           SessionRepository sessionRepo,
                           OneTimeTokenRepository tokenRepo,
                           LoginAttemptRepository loginAttemptRepo,
                           PlatformOperatorRepository operatorRepo,
                           AuditService auditService,
                           OutboxService outboxService,
                           IdempotencyService idempotencyService,
                           PasswordHasher passwordHasher,
                           IdentityNotificationPort notificationPort) {
        this.userRepo = userRepo;
        this.emailRepo = emailRepo;
        this.credentialRepo = credentialRepo;
        this.orgRepo = orgRepo;
        this.membershipRepo = membershipRepo;
        this.sessionRepo = sessionRepo;
        this.tokenRepo = tokenRepo;
        this.loginAttemptRepo = loginAttemptRepo;
        this.operatorRepo = operatorRepo;
        this.auditService = auditService;
        this.outboxService = outboxService;
        this.idempotencyService = idempotencyService;
        this.passwordHasher = passwordHasher;
        this.notificationPort = notificationPort;
    }

    // ==================== Registration ====================

    @Override
    @Transactional
    public RegistrationResult register(RegistrationCommand command, String clientIp, String userAgent, String requestId) {
        String emailNormalized = normalizeEmail(command.getEmail());
        long now = System.currentTimeMillis();

        // Idempotency check
        if (command.getIdempotencyKey() != null && !command.getIdempotencyKey().isEmpty()) {
            IdempotencyCommand idemCmd = new IdempotencyCommand();
            idemCmd.setScope("register");
            idemCmd.setIdempotencyKey(command.getIdempotencyKey());
            if (!idempotencyService.checkOrCreate(idemCmd)) {
                throw new AuthException("IDENTITY_IDEMPOTENCY_CONFLICT", "Duplicate registration request");
            }
        }

        // Check existing email
        emailRepo.findByNormalized(emailNormalized).ifPresent(existing -> {
            throw new AuthException("IDENTITY_EMAIL_ALREADY_REGISTERED",
                    "The email address is already registered. Please try logging in or reset your password.");
        });

        // Create User
        String userId = UUID.randomUUID().toString();
        User user = new User();
        user.setId(userId);
        user.setDisplayName(command.getDisplayName());
        user.setStatus("PENDING_VERIFICATION");
        user.setLocale("zh-CN");
        user.setTimezone("Asia/Shanghai");
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setVersion(1);
        userRepo.save(user);

        // Create UserEmail
        String emailId = UUID.randomUUID().toString();
        UserEmail email = new UserEmail();
        email.setId(emailId);
        email.setUserId(userId);
        email.setEmailNormalized(emailNormalized);
        email.setEmailDisplay(command.getEmail());
        email.setIsPrimary(1);
        email.setCreatedAt(now);
        email.setUpdatedAt(now);
        email.setVersion(1);
        emailRepo.save(email);

        // Create Credential
        String credentialId = UUID.randomUUID().toString();
        Credential credential = new Credential();
        credential.setId(credentialId);
        credential.setUserId(userId);
        credential.setCredentialType("PASSWORD");
        credential.setSecretHash(passwordHasher.hash(command.getPassword().toCharArray()));
        credential.setAlgorithm("BCRYPT");
        credential.setStatus("ACTIVE");
        credential.setMustChange(0);
        credential.setPasswordChangedAt(now);
        credential.setFailedAttemptCount(0);
        credential.setCreatedAt(now);
        credential.setUpdatedAt(now);
        credential.setVersion(1);
        credentialRepo.save(credential);

        // Create Personal Organization
        String orgId = UUID.randomUUID().toString();
        Organization org = new Organization();
        org.setId(orgId);
        org.setOrganizationType("PERSONAL");
        org.setName(command.getDisplayName() + "'s Workspace");
        org.setPersonalOwnerUserId(userId);
        org.setStatus("ACTIVE");
        org.setCreatedAt(now);
        org.setUpdatedAt(now);
        org.setVersion(1);
        orgRepo.save(org);

        // Create OWNER Membership
        String membershipId = UUID.randomUUID().toString();
        Membership membership = new Membership();
        membership.setId(membershipId);
        membership.setOrganizationId(orgId);
        membership.setUserId(userId);
        membership.setMembershipType("OWNER");
        membership.setStatus("ACTIVE");
        membership.setJoinedAt(now);
        membership.setCreatedAt(now);
        membership.setUpdatedAt(now);
        membership.setVersion(1);
        membershipRepo.save(membership);

        // Create email verification token
        String rawToken = generateRandomToken();
        String tokenHash = hashToken(rawToken);
        String tokenId = UUID.randomUUID().toString();
        OneTimeToken oneTimeToken = new OneTimeToken();
        oneTimeToken.setId(tokenId);
        oneTimeToken.setUserId(userId);
        oneTimeToken.setTokenType("EMAIL_VERIFICATION");
        oneTimeToken.setTokenHash(tokenHash);
        oneTimeToken.setStatus("ACTIVE");
        oneTimeToken.setExpiresAt(now + TOKEN_EXPIRE_MINUTES * 60 * 1000L);
        oneTimeToken.setCreatedAt(now);
        oneTimeToken.setUpdatedAt(now);
        oneTimeToken.setVersion(1);
        tokenRepo.save(oneTimeToken);

        // Audit
        writeAudit("IDENTITY_USER_REGISTERED", "USER", userId, "REGISTER", "USER", userId,
                "SUCCESS", null, requestId, clientIp, userAgent);

        // Outbox
        writeOutbox("identity.user.registered", "User", userId,
                "{\"userId\":\"" + userId + "\",\"organizationId\":\"" + orgId + "\",\"status\":\"PENDING_VERIFICATION\"}");

        // Notification (outside transaction effect, best-effort)
        try {
            String verificationUrl = "https://localhost:5173/verify-email?token=" + rawToken;
            notificationPort.sendEmailVerification(command.getEmail(), command.getDisplayName(), rawToken, verificationUrl);
        } catch (Exception e) {
            log.warn("Failed to send verification email to {}: {}", command.getEmail(), e.getMessage());
        }

        log.info("User registered: {} ({})", userId, command.getEmail());
        return new RegistrationResult(userId, orgId, maskEmail(command.getEmail()));
    }

    // ==================== Email Verification ====================

    @Override
    @Transactional
    public VerificationResult verifyEmail(String token) {
        String tokenHash = hashToken(token);
        OneTimeToken ott = tokenRepo.findByTokenHash(tokenHash)
                .orElseThrow(() -> new AuthException("IDENTITY_EMAIL_VERIFICATION_INVALID", "Invalid verification token"));

        if ("USED".equals(ott.getStatus())) {
            return new VerificationResult(true, "Email was already verified", ott.getUserId());
        }
        if ("REVOKED".equals(ott.getStatus())) {
            throw new AuthException("IDENTITY_EMAIL_VERIFICATION_INVALID", "Verification token has been revoked");
        }
        if (ott.getExpiresAt() < System.currentTimeMillis()) {
            throw new AuthException("IDENTITY_EMAIL_VERIFICATION_EXPIRED", "Verification token has expired");
        }

        long now = System.currentTimeMillis();
        tokenRepo.markUsed(ott.getId(), now, ott.getVersion());

        // Mark email verified
        emailRepo.findByUserId(ott.getUserId()).ifPresent(email -> {
            emailRepo.markVerified(email.getId(), now, email.getVersion());
        });

        // Activate user
        User user = userRepo.findById(ott.getUserId())
                .orElseThrow(() -> new AuthException("IDENTITY_USER_NOT_FOUND", "User not found"));
        user.setStatus("ACTIVE");
        user.setUpdatedAt(now);
        userRepo.update(user);

        // Audit
        writeAudit("IDENTITY_EMAIL_VERIFIED", "USER", ott.getUserId(), "VERIFY_EMAIL", "USER_EMAIL", ott.getUserId(),
                "SUCCESS", null, null, null, null);

        // Outbox
        writeOutbox("identity.user.email_verified", "User", ott.getUserId(),
                "{\"userId\":\"" + ott.getUserId() + "\"}");
        writeOutbox("identity.user.activated", "User", ott.getUserId(),
                "{\"userId\":\"" + ott.getUserId() + "\",\"status\":\"ACTIVE\"}");

        log.info("Email verified for user: {}", ott.getUserId());
        return new VerificationResult(true, "Email verified successfully. You can now log in.", ott.getUserId());
    }

    // ==================== Resend Verification ====================

    @Override
    @Transactional
    public void resendEmailVerification(String email) {
        String emailNormalized = normalizeEmail(email);
        UserEmail userEmail = emailRepo.findByNormalized(emailNormalized)
                .orElseThrow(() -> new AuthException("IDENTITY_USER_NOT_FOUND", "If this email is registered, a verification email has been sent."));

        User user = userRepo.findById(userEmail.getUserId())
                .orElseThrow(() -> new AuthException("IDENTITY_USER_NOT_FOUND", "If this email is registered, a verification email has been sent."));

        if (!"PENDING_VERIFICATION".equals(user.getStatus())) {
            throw new AuthException("IDENTITY_EMAIL_ALREADY_REGISTERED", "Email is already verified");
        }

        // Revoke old tokens
        tokenRepo.revokeAllForUserAndType(user.getId(), "EMAIL_VERIFICATION");

        // Create new token
        long now = System.currentTimeMillis();
        String rawToken = generateRandomToken();
        String tokenHash = hashToken(rawToken);
        OneTimeToken oneTimeToken = new OneTimeToken();
        oneTimeToken.setId(UUID.randomUUID().toString());
        oneTimeToken.setUserId(user.getId());
        oneTimeToken.setTokenType("EMAIL_VERIFICATION");
        oneTimeToken.setTokenHash(tokenHash);
        oneTimeToken.setStatus("ACTIVE");
        oneTimeToken.setExpiresAt(now + TOKEN_EXPIRE_MINUTES * 60 * 1000L);
        oneTimeToken.setCreatedAt(now);
        oneTimeToken.setUpdatedAt(now);
        oneTimeToken.setVersion(1);
        tokenRepo.save(oneTimeToken);

        // Notification
        try {
            String verificationUrl = "https://localhost:5173/verify-email?token=" + rawToken;
            notificationPort.sendEmailVerification(email, user.getDisplayName(), rawToken, verificationUrl);
        } catch (Exception e) {
            log.warn("Failed to send verification email: {}", e.getMessage());
        }

        writeAudit("IDENTITY_EMAIL_VERIFICATION_SENT", "SYSTEM", "system", "RESEND_VERIFICATION", "USER", user.getId(),
                "SUCCESS", null, null, null, null);
    }

    // ==================== Login ====================

    @Override
    @Transactional
    public LoginResult login(LoginCommand command, String clientIp, String userAgent, String requestId) {
        String emailNormalized = normalizeEmail(command.getEmail());
        long now = System.currentTimeMillis();
        String emailHash = hashEmail(emailNormalized);

        // Rate limit check (IP)
        int ipCount = loginAttemptRepo.countRecentAttemptsByIp(clientIp, now - 60_000);
        if (ipCount > 20) {
            writeLoginAttempt(null, emailHash, "BLOCKED", "RATE_LIMIT_IP", clientIp, userAgent, requestId, now);
            throw new AuthException("IDENTITY_RATE_LIMITED", "Too many login attempts. Please try again later.");
        }

        // Find user by email
        UserEmail userEmail = emailRepo.findByNormalized(emailNormalized).orElse(null);
        if (userEmail == null) {
            writeLoginAttempt(null, emailHash, "FAILURE", "EMAIL_NOT_FOUND", clientIp, userAgent, requestId, now);
            throw new AuthException("IDENTITY_INVALID_CREDENTIALS", "Email or password is incorrect.");
        }

        String userId = userEmail.getUserId();

        // Check user status
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new AuthException("IDENTITY_INVALID_CREDENTIALS", "Email or password is incorrect."));

        if ("DISABLED".equals(user.getStatus())) {
            throw new AuthException("IDENTITY_ACCOUNT_DISABLED", "Your account has been disabled.");
        }

        if ("LOCKED".equals(user.getStatus())) {
            if (user.getLockedUntil() != null && user.getLockedUntil() > now) {
                throw new AuthException("IDENTITY_ACCOUNT_LOCKED", "Your account is temporarily locked. Please try again later.");
            }
            // Lock expired, restore to previous state
            user.setStatus(userEmail.getVerifiedAt() != null ? "ACTIVE" : "PENDING_VERIFICATION");
            user.setLockedUntil(null);
            userRepo.update(user);
        }

        if ("PENDING_VERIFICATION".equals(user.getStatus())) {
            throw new AuthException("IDENTITY_EMAIL_NOT_VERIFIED", "Please verify your email before logging in.");
        }

        // Check rate limit (user)
        int recentFailures = loginAttemptRepo.countRecentFailuresByUser(userId, now - 15 * 60 * 1000L);
        if (recentFailures >= MAX_FAILED_ATTEMPTS) {
            user.setStatus("LOCKED");
            user.setLockedUntil(now + LOCK_DURATION_MS);
            user.setUpdatedAt(now);
            userRepo.update(user);
            writeLoginAttempt(userId, emailHash, "BLOCKED", "MAX_ATTEMPTS", clientIp, userAgent, requestId, now);
            throw new AuthException("IDENTITY_ACCOUNT_LOCKED", "Account temporarily locked due to too many failed attempts.");
        }

        // Verify password
        Credential credential = credentialRepo.findByUserIdAndType(userId, "PASSWORD")
                .orElseThrow(() -> new AuthException("IDENTITY_INVALID_CREDENTIALS", "Email or password is incorrect."));

        if (!passwordHasher.matches(command.getPassword().toCharArray(), credential.getSecretHash())) {
            int newCount = credential.getFailedAttemptCount() + 1;
            credentialRepo.incrementFailedAttempts(credential.getId(), newCount, credential.getVersion());
            writeLoginAttempt(userId, emailHash, "FAILURE", "PASSWORD_MISMATCH", clientIp, userAgent, requestId, now);

            if (newCount >= MAX_FAILED_ATTEMPTS) {
                user.setStatus("LOCKED");
                user.setLockedUntil(now + LOCK_DURATION_MS);
                user.setUpdatedAt(now);
                userRepo.update(user);
            }
            throw new AuthException("IDENTITY_INVALID_CREDENTIALS", "Email or password is incorrect.");
        }

        // Login success
        // Reset failed attempts
        credential.setFailedAttemptCount(0);
        credentialRepo.incrementFailedAttempts(credential.getId(), 0, credential.getVersion());

        // Update last login
        user.setLastLoginAt(now);
        user.setUpdatedAt(now);
        userRepo.update(user);

        // Handle "must_change" flag
        if (credential.getMustChange() == 1) {
            throw new MustChangePasswordException(userId, "You must change your password before continuing.");
        }

        // Check rehash
        if (passwordHasher.needsRehash(credential.getSecretHash())) {
            String newHash = passwordHasher.hash(command.getPassword().toCharArray());
            credentialRepo.updatePassword(credential.getId(), newHash, "BCRYPT", credential.getPasswordChangedAt(), credential.getVersion());
        }

        // Create session
        String rawToken = generateRandomToken();
        String sessionTokenHash = hashToken(rawToken);
        Session session = createSession(userId, "USER_WEB", sessionTokenHash, clientIp, userAgent, now, false);

        // Audit
        writeAudit("IDENTITY_LOGIN_SUCCEEDED", "USER", userId, "LOGIN", "SESSION", session.getId(),
                "SUCCESS", null, requestId, clientIp, userAgent);
        writeLoginAttempt(userId, emailHash, "SUCCESS", null, clientIp, userAgent, requestId, now);

        // Outbox
        writeOutbox("identity.session.created", "Session", session.getId(),
                "{\"userId\":\"" + userId + "\",\"sessionId\":\"" + session.getId() + "\"}");

        log.info("Login succeeded: {}", userId);
        return new LoginResult(userId, rawToken, user.getDisplayName(), userEmail.getEmailDisplay(),
                findPersonalOrg(userId));
    }

    // ==================== Logout ====================

    @Override
    @Transactional
    public void logout(String sessionToken) {
        String tokenHash = hashToken(sessionToken);
        Session session = sessionRepo.findByTokenHash(tokenHash)
                .orElseThrow(() -> new AuthException("IDENTITY_SESSION_INVALID", "Invalid session"));

        long now = System.currentTimeMillis();
        session.setStatus("REVOKED");
        session.setRevokedAt(now);
        session.setRevokeReason("USER_LOGOUT");
        session.setUpdatedAt(now);
        sessionRepo.update(session);

        writeAudit("IDENTITY_LOGOUT", "USER", session.getUserId(), "LOGOUT", "SESSION", session.getId(),
                "SUCCESS", null, null, null, null);

        log.info("Session revoked (logout): {}", session.getId());
    }

    // ==================== Change Password ====================

    @Override
    @Transactional
    public void changePassword(String userId, PasswordChangeCommand command, String requestId) {
        long now = System.currentTimeMillis();

        Credential credential = credentialRepo.findByUserIdAndType(userId, "PASSWORD")
                .orElseThrow(() -> new AuthException("IDENTITY_INVALID_CREDENTIALS", "Credential not found"));

        if (!passwordHasher.matches(command.getCurrentPassword().toCharArray(), credential.getSecretHash())) {
            throw new AuthException("IDENTITY_CURRENT_PASSWORD_INVALID", "Current password is incorrect");
        }

        if (command.getCurrentPassword().equals(command.getNewPassword())) {
            throw new AuthException("IDENTITY_PASSWORD_POLICY_VIOLATION", "New password must be different from current password");
        }

        String newHash = passwordHasher.hash(command.getNewPassword().toCharArray());
        credentialRepo.updatePassword(credential.getId(), newHash, "BCRYPT", now, credential.getVersion());

        // Revoke other sessions
        String currentSessionId = getCurrentSessionId(userId);
        sessionRepo.revokeExceptCurrent(userId, currentSessionId != null ? currentSessionId : "",
                "PASSWORD_CHANGED", now);

        // Revoke other tokens
        tokenRepo.revokeAllForUser(userId);

        writeAudit("IDENTITY_PASSWORD_CHANGED", "USER", userId, "CHANGE_PASSWORD", "CREDENTIAL", credential.getId(),
                "SUCCESS", null, requestId, null, null);

        // Notification
        emailRepo.findByUserId(userId).ifPresent(userEmail -> {
            userRepo.findById(userId).ifPresent(user -> {
                try {
                    notificationPort.sendPasswordChanged(userEmail.getEmailDisplay(), user.getDisplayName());
                } catch (Exception e) {
                    log.warn("Failed to send password change notification: {}", e.getMessage());
                }
            });
        });

        log.info("Password changed for user: {}", userId);
    }

    // ==================== Password Reset ====================

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        String emailNormalized = normalizeEmail(email);
        UserEmail userEmail = emailRepo.findByNormalized(emailNormalized).orElse(null);
        if (userEmail == null) {
            log.info("Password reset requested for unknown email, silent return");
            return; // Silent return for security
        }

        User user = userRepo.findById(userEmail.getUserId()).orElse(null);
        if (user == null) {
            return;
        }

        // Revoke old reset tokens
        tokenRepo.revokeAllForUserAndType(user.getId(), "PASSWORD_RESET");

        long now = System.currentTimeMillis();
        String rawToken = generateRandomToken();
        String tokenHash = hashToken(rawToken);
        OneTimeToken token = new OneTimeToken();
        token.setId(UUID.randomUUID().toString());
        token.setUserId(user.getId());
        token.setTokenType("PASSWORD_RESET");
        token.setTokenHash(tokenHash);
        token.setStatus("ACTIVE");
        token.setExpiresAt(now + TOKEN_EXPIRE_MINUTES * 60 * 1000L);
        token.setCreatedAt(now);
        token.setUpdatedAt(now);
        token.setVersion(1);
        tokenRepo.save(token);

        writeAudit("IDENTITY_PASSWORD_RESET_REQUESTED", "SYSTEM", "system", "REQUEST_PASSWORD_RESET", "USER",
                user.getId(), "SUCCESS", null, null, null, null);

        try {
            String resetUrl = "https://localhost:5173/reset-password?token=" + rawToken;
            notificationPort.sendPasswordReset(email, user.getDisplayName(), rawToken, resetUrl);
        } catch (Exception e) {
            log.warn("Failed to send password reset email: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void completePasswordReset(PasswordResetCommand command, String requestId, String clientIp) {
        String emailNormalized = normalizeEmail(command.getEmail());
        String tokenHash = hashToken(command.getToken());

        OneTimeToken ott = tokenRepo.findByTokenHash(tokenHash)
                .orElseThrow(() -> new AuthException("IDENTITY_PASSWORD_RESET_INVALID", "Invalid or expired reset token"));

        if (!"ACTIVE".equals(ott.getStatus())) {
            throw new AuthException("IDENTITY_PASSWORD_RESET_INVALID", "Reset token already used");
        }
        if (ott.getExpiresAt() < System.currentTimeMillis()) {
            throw new AuthException("IDENTITY_PASSWORD_RESET_EXPIRED", "Reset token has expired");
        }

        // Verify email matches
        UserEmail userEmail = emailRepo.findByUserId(ott.getUserId())
                .orElseThrow(() -> new AuthException("IDENTITY_USER_NOT_FOUND", "User not found"));
        if (!userEmail.getEmailNormalized().equals(emailNormalized)) {
            throw new AuthException("IDENTITY_PASSWORD_RESET_INVALID", "Invalid reset token");
        }

        long now = System.currentTimeMillis();

        // Mark token used
        tokenRepo.markUsed(ott.getId(), now, ott.getVersion());

        // Update password
        String newHash = passwordHasher.hash(command.getNewPassword().toCharArray());
        Credential credential = credentialRepo.findByUserIdAndType(ott.getUserId(), "PASSWORD")
                .orElseThrow(() -> new AuthException("IDENTITY_USER_NOT_FOUND", "User not found"));
        credentialRepo.updatePassword(credential.getId(), newHash, "BCRYPT", now, credential.getVersion());

        // Revoke all sessions
        sessionRepo.revokeByUserId(ott.getUserId(), "PASSWORD_RESET", now);

        // Revoke all other tokens
        tokenRepo.revokeAllForUser(ott.getUserId());

        // Restore token mark
        // Already done by updatePassword which also resets failed counts

        writeAudit("IDENTITY_PASSWORD_RESET_COMPLETED", "USER", ott.getUserId(), "COMPLETE_PASSWORD_RESET",
                "CREDENTIAL", credential.getId(), "SUCCESS", null, requestId, clientIp, null);

        writeOutbox("identity.password.reset", "User", ott.getUserId(),
                "{\"userId\":\"" + ott.getUserId() + "\"}");

        log.info("Password reset completed for user: {}", ott.getUserId());
    }

    // ==================== Session Introspection ====================

    @Override
    public String introspectSession(String sessionToken) {
        String tokenHash = hashToken(sessionToken);
        Session session = sessionRepo.findByTokenHash(tokenHash).orElse(null);
        if (session == null) {
            return null;
        }

        long now = System.currentTimeMillis();
        if (!"ACTIVE".equals(session.getStatus())) {
            return null;
        }
        if (session.getAbsoluteExpiresAt() < now) {
            return null;
        }
        if (session.getIdleExpiresAt() < now) {
            return null;
        }

        // Update last active and extend idle
        session.setLastActiveAt(now);
        session.setIdleExpiresAt(now + SESSION_IDLE_HOURS * 3600 * 1000L);
        session.setUpdatedAt(now);
        sessionRepo.update(session);

        return session.getUserId();
    }

    // ==================== Helpers ====================

    private Session createSession(String userId, String sessionType, String tokenHash,
                                   String ipAddress, String userAgent, long now, boolean rememberMe) {
        Session session = new Session();
        session.setId(UUID.randomUUID().toString());
        session.setUserId(userId);
        session.setSessionType(sessionType);
        session.setTokenHash(tokenHash);
        session.setStatus("ACTIVE");
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setDeviceName(deriveDeviceName(userAgent));
        session.setLastActiveAt(now);
        session.setIdleExpiresAt(now + SESSION_IDLE_HOURS * 3600 * 1000L);
        session.setAbsoluteExpiresAt(now + SESSION_ABSOLUTE_HOURS * 3600 * 1000L);
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        session.setVersion(1);
        sessionRepo.save(session);
        return session;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) return "***";
        return email.charAt(0) + "***" + email.substring(atIndex);
    }

    private String generateRandomToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private String hashEmail(String email) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest((email + "identity-salt").getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 64);
        } catch (NoSuchAlgorithmException e) {
            return email;
        }
    }

    private String deriveDeviceName(String userAgent) {
        if (userAgent == null) return "Unknown";
        if (userAgent.contains("Windows")) return "Windows";
        if (userAgent.contains("Mac")) return "Mac";
        if (userAgent.contains("Linux")) return "Linux";
        if (userAgent.contains("Android")) return "Android";
        if (userAgent.contains("iPhone") || userAgent.contains("iPad")) return "iOS";
        return "Other";
    }

    private String findPersonalOrg(String userId) {
        return orgRepo.findByPersonalOwner(userId).map(Organization::getId).orElse(null);
    }

    private String getCurrentSessionId(String userId) {
        List<Session> sessions = sessionRepo.findByUserIdAndStatus(userId, "ACTIVE");
        return sessions.isEmpty() ? null : sessions.get(0).getId();
    }

    private void writeLoginAttempt(String userId, String emailHash, String result, String failureReason,
                                    String ip, String userAgent, String requestId, long now) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setId(UUID.randomUUID().toString());
        attempt.setUserId(userId);
        attempt.setEmailHash(emailHash);
        attempt.setResult(result);
        attempt.setFailureReason(failureReason);
        attempt.setIpAddress(ip);
        attempt.setUserAgent(userAgent);
        attempt.setRequestId(requestId);
        attempt.setOccurredAt(now);
        loginAttemptRepo.save(attempt);
    }

    private void writeAudit(String eventType, String actorType, String actorId, String action,
                            String targetType, String targetId, String result, String reason,
                            String requestId, String sourceIp, String userAgent) {
        try {
            AuditCommand audit = new AuditCommand();
            audit.setEventType(eventType);
            audit.setActorType(actorType);
            audit.setActorId(actorId);
            audit.setAction(action);
            audit.setTargetType(targetType);
            audit.setTargetId(targetId);
            audit.setResult(result);
            audit.setReason(reason);
            audit.setRequestId(requestId);
            audit.setSourceIp(sourceIp);
            audit.setUserAgent(userAgent);
            auditService.record(audit);
        } catch (Exception e) {
            log.warn("Failed to write audit: {}", e.getMessage());
        }
    }

    private void writeOutbox(String eventType, String aggregateType, String aggregateId, String payloadJson) {
        try {
            OutboxCommand cmd = new OutboxCommand();
            cmd.setEventType(eventType);
            cmd.setAggregateType(aggregateType);
            cmd.setAggregateId(aggregateId);
            cmd.setPayloadJson(payloadJson);
            outboxService.write(cmd);
        } catch (Exception e) {
            log.warn("Failed to write outbox: {}", e.getMessage());
        }
    }

    // ==================== Exceptions ====================

    public static class AuthException extends RuntimeException {
        private final String errorCode;

        public AuthException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }

        public String getErrorCode() { return errorCode; }
    }

    public static class MustChangePasswordException extends AuthException {
        private final String userId;

        public MustChangePasswordException(String userId, String message) {
            super("IDENTITY_PASSWORD_MUST_CHANGE", message);
            this.userId = userId;
        }

        public String getUserId() { return userId; }
    }
}