package com.github.houbb.core.identity;

import com.github.houbb.core.identity.application.command.LoginCommand;
import com.github.houbb.core.identity.application.command.PasswordChangeCommand;
import com.github.houbb.core.identity.application.command.PasswordResetCommand;
import com.github.houbb.core.identity.application.command.RegistrationCommand;
import com.github.houbb.core.identity.application.domain.*;
import com.github.houbb.core.identity.application.port.*;
import com.github.houbb.core.identity.application.service.*;
import com.github.houbb.core.identity.application.service.AuthServiceImpl.*;
import com.github.houbb.core.identity.infrastructure.security.BCryptPasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("P1 Identity MVP Unit Tests")
class P1IdentityUnitTest {

    private StubUserRepository userRepo;
    private StubUserEmailRepository emailRepo;
    private StubCredentialRepository credentialRepo;
    private StubOrganizationRepository orgRepo;
    private StubMembershipRepository membershipRepo;
    private StubSessionRepository sessionRepo;
    private StubOneTimeTokenRepository tokenRepo;
    private StubLoginAttemptRepository loginAttemptRepo;
    private StubPlatformOperatorRepository operatorRepo;
    private StubAuditEventRepository auditRepo;
    private StubOutboxEventRepository outboxRepo;
    private AuditService auditService;
    private OutboxService outboxService;
    private IdempotencyService idempotencyService;
    private PasswordHasher passwordHasher;
    private StubNotificationPort notificationPort;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepo = new StubUserRepository();
        emailRepo = new StubUserEmailRepository();
        credentialRepo = new StubCredentialRepository();
        orgRepo = new StubOrganizationRepository();
        membershipRepo = new StubMembershipRepository();
        sessionRepo = new StubSessionRepository();
        tokenRepo = new StubOneTimeTokenRepository();
        loginAttemptRepo = new StubLoginAttemptRepository();
        operatorRepo = new StubPlatformOperatorRepository();
        auditRepo = new StubAuditEventRepository();
        outboxRepo = new StubOutboxEventRepository();

        auditService = new AuditServiceImpl(auditRepo);
        outboxService = new OutboxServiceImpl(outboxRepo);
        idempotencyService = new IdempotencyServiceImpl(new StubIdempotencyRecordRepository(), 24);
        passwordHasher = new BCryptPasswordHasher();
        notificationPort = new StubNotificationPort();

        authService = new AuthServiceImpl(userRepo, emailRepo, credentialRepo, orgRepo, membershipRepo,
                sessionRepo, tokenRepo, loginAttemptRepo, operatorRepo,
                auditService, outboxService, idempotencyService, passwordHasher, notificationPort);
    }

    @Nested
    @DisplayName("Registration")
    class RegistrationTests {

        @Test
        @DisplayName("should register a new user and create personal org")
        void shouldRegisterNewUser() {
            RegistrationCommand cmd = new RegistrationCommand();
            cmd.setEmail("test@example.com");
            cmd.setPassword("password123");
            cmd.setDisplayName("Test User");

            AuthService.RegistrationResult result = authService.register(cmd, "127.0.0.1", "ua", "req-1");

            assertNotNull(result.userId());
            assertNotNull(result.organizationId());
            assertTrue(result.emailMasked().contains("***"));

            User user = userRepo.findById(result.userId()).orElseThrow();
            assertEquals("PENDING_VERIFICATION", user.getStatus());
            assertEquals("Test User", user.getDisplayName());

            // Check org was created
            Organization org = orgRepo.findByPersonalOwner(result.userId()).orElseThrow();
            assertEquals("PERSONAL", org.getOrganizationType());

            // Check membership
            List<Membership> memberships = membershipRepo.findByUserId(result.userId());
            assertEquals(1, memberships.size());
            assertEquals("OWNER", memberships.get(0).getMembershipType());

            // Check credential
            Credential cred = credentialRepo.findByUserIdAndType(result.userId(), "PASSWORD").orElseThrow();
            assertNotNull(cred.getSecretHash());
            assertNotEquals("password123", cred.getSecretHash());

            // Check email record
            UserEmail email = emailRepo.findByNormalized("test@example.com").orElseThrow();
            assertNull(email.getVerifiedAt());
        }

        @Test
        @DisplayName("should reject duplicate email")
        void shouldRejectDuplicateEmail() {
            RegistrationCommand cmd1 = new RegistrationCommand();
            cmd1.setEmail("dup@example.com");
            cmd1.setPassword("password123");
            cmd1.setDisplayName("User 1");
            authService.register(cmd1, "127.0.0.1", "ua", "req-1");

            RegistrationCommand cmd2 = new RegistrationCommand();
            cmd2.setEmail("dup@example.com");
            cmd2.setPassword("password456");
            cmd2.setDisplayName("User 2");

            assertThrows(AuthException.class,
                    () -> authService.register(cmd2, "127.0.0.1", "ua", "req-2"));
        }
    }

    @Nested
    @DisplayName("Email Verification")
    class EmailVerificationTests {

        @Test
        @DisplayName("should verify email and activate user")
        void shouldVerifyEmail() {
            // First register
            RegistrationCommand cmd = new RegistrationCommand();
            cmd.setEmail("verify@example.com");
            cmd.setPassword("password123");
            cmd.setDisplayName("Verify Me");
            authService.register(cmd, "127.0.0.1", "ua", "req-1");

            // The notification port stores the last token
            String rawToken = notificationPort.lastVerificationToken;
            assertNotNull(rawToken);

            AuthService.VerificationResult result = authService.verifyEmail(rawToken);
            assertTrue(result.success());

            // User should now be ACTIVE
            User user = userRepo.findById(result.userId()).orElseThrow();
            assertEquals("ACTIVE", user.getStatus());
        }

        @Test
        @DisplayName("should reject invalid token")
        void shouldRejectInvalidToken() {
            assertThrows(AuthException.class, () -> authService.verifyEmail("invalid-token"));
        }
    }

    @Nested
    @DisplayName("Login")
    class LoginTests {

        private void createActiveUser(String email, String password, String displayName) {
            RegistrationCommand cmd = new RegistrationCommand();
            cmd.setEmail(email);
            cmd.setPassword(password);
            cmd.setDisplayName(displayName);
            authService.register(cmd, "127.0.0.1", "ua", "req-1");

            String token = notificationPort.lastVerificationToken;
            authService.verifyEmail(token);
        }

        @Test
        @DisplayName("should login with correct credentials")
        void shouldLoginWithCorrectCredentials() {
            createActiveUser("login@example.com", "password123", "Login User");

            LoginCommand cmd = new LoginCommand();
            cmd.setEmail("login@example.com");
            cmd.setPassword("password123");

            AuthService.LoginResult result = authService.login(cmd, "127.0.0.1", "ua", "req-2");
            assertNotNull(result.sessionToken());
            assertEquals("Login User", result.displayName());
        }

        @Test
        @DisplayName("should reject wrong password")
        void shouldRejectWrongPassword() {
            createActiveUser("wrong@example.com", "password123", "Wrong User");

            LoginCommand cmd = new LoginCommand();
            cmd.setEmail("wrong@example.com");
            cmd.setPassword("wrongpassword");

            assertThrows(AuthException.class,
                    () -> authService.login(cmd, "127.0.0.1", "ua", "req-2"));
        }

        @Test
        @DisplayName("should reject unverified user")
        void shouldRejectUnverifiedUser() {
            RegistrationCommand cmd = new RegistrationCommand();
            cmd.setEmail("unverified@example.com");
            cmd.setPassword("password123");
            cmd.setDisplayName("Unverified");
            authService.register(cmd, "127.0.0.1", "ua", "req-1");

            LoginCommand loginCmd = new LoginCommand();
            loginCmd.setEmail("unverified@example.com");
            loginCmd.setPassword("password123");

            assertThrows(AuthException.class,
                    () -> authService.login(loginCmd, "127.0.0.1", "ua", "req-2"));
        }
    }

    @Nested
    @DisplayName("Password")
    class PasswordTests {

        @Test
        @DisplayName("password hasher should work correctly")
        void passwordHasherShouldWork() {
            String hash = passwordHasher.hash("test_password".toCharArray());
            assertNotNull(hash);
            assertNotEquals("test_password", hash);
            assertTrue(passwordHasher.matches("test_password".toCharArray(), hash));
            assertFalse(passwordHasher.matches("wrong_password".toCharArray(), hash));
        }

        @Test
        @DisplayName("should detect needsRehash")
        void shouldDetectNeedsRehash() {
            String hash = passwordHasher.hash("password".toCharArray());
            assertFalse(passwordHasher.needsRehash(hash));
        }
    }

    // === Stubs ===

    static class StubUserRepository implements UserRepository {
        final Map<String, User> users = new LinkedHashMap<>();
        @Override public void save(User user) { users.put(user.getId(), user); }
        @Override public Optional<User> findById(String id) { return Optional.ofNullable(users.get(id)); }
        @Override public void update(User user) { users.put(user.getId(), user); }
        @Override public void updateStatus(String id, String status, long version) {
            User u = users.get(id); if (u != null) { u.setStatus(status); u.setUpdatedAt(System.currentTimeMillis()); }
        }
    }

    static class StubUserEmailRepository implements UserEmailRepository {
        final Map<String, UserEmail> byNormalized = new LinkedHashMap<>();
        final Map<String, UserEmail> byUserId = new LinkedHashMap<>();
        @Override public void save(UserEmail email) { byNormalized.put(email.getEmailNormalized(), email); byUserId.put(email.getUserId(), email); }
        @Override public Optional<UserEmail> findByNormalized(String normalized) { return Optional.ofNullable(byNormalized.get(normalized)); }
        @Override public Optional<UserEmail> findByUserId(String userId) { return Optional.ofNullable(byUserId.get(userId)); }
        @Override public void update(UserEmail email) { save(email); }
        @Override public void markVerified(String id, long verifiedAt, long version) {
            byUserId.values().forEach(e -> { if (e.getId().equals(id)) e.setVerifiedAt(verifiedAt); });
        }
    }

    static class StubCredentialRepository implements CredentialRepository {
        final Map<String, Credential> byUserType = new LinkedHashMap<>();
        @Override public void save(Credential c) { byUserType.put(c.getUserId() + ":" + c.getCredentialType(), c); }
        @Override public Optional<Credential> findByUserIdAndType(String uid, String type) {
            return Optional.ofNullable(byUserType.get(uid + ":" + type));
        }
        @Override public void update(Credential c) { save(c); }
        @Override public void incrementFailedAttempts(String id, int newCount, long version) {
            byUserType.values().forEach(c -> { if (c.getId().equals(id)) c.setFailedAttemptCount(newCount); });
        }
        @Override public void updatePassword(String id, String hash, String algo, long ts, long version) {
            byUserType.values().forEach(c -> { if (c.getId().equals(id)) { c.setSecretHash(hash); c.setPasswordChangedAt(ts); c.setFailedAttemptCount(0); }});
        }
        @Override public void revokeByUserId(String userId) {}
    }

    static class StubOrganizationRepository implements OrganizationRepository {
        final Map<String, Organization> byId = new LinkedHashMap<>();
        final Map<String, Organization> byOwner = new LinkedHashMap<>();
        @Override public void save(Organization o) { byId.put(o.getId(), o); if (o.getPersonalOwnerUserId() != null) byOwner.put(o.getPersonalOwnerUserId(), o); }
        @Override public Optional<Organization> findById(String id) { return Optional.ofNullable(byId.get(id)); }
        @Override public Optional<Organization> findByPersonalOwner(String uid) { return Optional.ofNullable(byOwner.get(uid)); }
        @Override public Optional<Organization> findBySlug(String slug) { return byId.values().stream().filter(o -> slug.equals(o.getSlug())).findFirst(); }
        @Override public void update(Organization o) { save(o); }
    }

    static class StubMembershipRepository implements MembershipRepository {
        final List<Membership> memberships = new ArrayList<>();
        @Override public void save(Membership m) { memberships.add(m); }
        @Override public Optional<Membership> findByOrgAndUser(String oid, String uid) {
            return memberships.stream().filter(m -> oid.equals(m.getOrganizationId()) && uid.equals(m.getUserId())).findFirst();
        }
        @Override public List<Membership> findByUserId(String uid) {
            return memberships.stream().filter(m -> uid.equals(m.getUserId())).toList();
        }
        @Override public List<Membership> findByOrganizationId(String oid) { return new ArrayList<>(); }
        @Override public void update(Membership m) {}
    }

    static class StubSessionRepository implements SessionRepository {
        final List<Session> sessions = new ArrayList<>();
        @Override public void save(Session s) { sessions.add(s); }
        @Override public Optional<Session> findByTokenHash(String hash) {
            return sessions.stream().filter(s -> hash.equals(s.getTokenHash())).findFirst();
        }
        @Override public Optional<Session> findById(String id) {
            return sessions.stream().filter(s -> id.equals(s.getId())).findFirst();
        }
        @Override public List<Session> findByUserIdAndStatus(String uid, String status) {
            return sessions.stream().filter(s -> uid.equals(s.getUserId()) && status.equals(s.getStatus())).toList();
        }
        @Override public List<Session> findActiveByUserId(String uid, long now) {
            return sessions.stream().filter(s -> uid.equals(s.getUserId()) && "ACTIVE".equals(s.getStatus())).toList();
        }
        @Override public void update(Session s) {}
        @Override public void revokeByUserId(String uid, String reason, long ts) {}
        @Override public void revokeExceptCurrent(String uid, String sid, String reason, long ts) {}
        @Override public void expireIdle(long ts) {}
    }

    static class StubOneTimeTokenRepository implements OneTimeTokenRepository {
        final Map<String, OneTimeToken> byHash = new LinkedHashMap<>();
        @Override public void save(OneTimeToken t) { byHash.put(t.getTokenHash(), t); }
        @Override public Optional<OneTimeToken> findByTokenHash(String hash) { return Optional.ofNullable(byHash.get(hash)); }
        @Override public Optional<OneTimeToken> findActiveByUserAndType(String uid, String type) { return Optional.empty(); }
        @Override public void markUsed(String id, long usedAt, long version) {
            byHash.values().forEach(t -> { if (t.getId().equals(id)) t.setStatus("USED"); });
        }
        @Override public void revokeAllForUser(String uid) {}
        @Override public void revokeAllForUserAndType(String uid, String type) {}
        @Override public void expireBefore(long ts) {}
    }

    static class StubLoginAttemptRepository implements LoginAttemptRepository {
        final List<LoginAttempt> attempts = new ArrayList<>();
        @Override public void save(LoginAttempt a) { attempts.add(a); }
        @Override public int countRecentFailuresByUser(String uid, long since) {
            return (int) attempts.stream().filter(a -> uid != null && uid.equals(a.getUserId()) && "FAILURE".equals(a.getResult())).count();
        }
        @Override public int countRecentAttemptsByIp(String ip, long since) { return 0; }
        @Override public List<LoginAttempt> findByUserId(String uid, int limit) { return new ArrayList<>(); }
    }

    static class StubPlatformOperatorRepository implements PlatformOperatorRepository {
        final Map<String, PlatformOperator> byUserId = new LinkedHashMap<>();
        @Override public void save(PlatformOperator o) { byUserId.put(o.getUserId(), o); }
        @Override public Optional<PlatformOperator> findByUserId(String uid) { return Optional.ofNullable(byUserId.get(uid)); }
        @Override public void update(PlatformOperator o) { save(o); }
        @Override public void disable(String id, long ts, long version) {}
    }

    static class StubAuditEventRepository implements AuditEventRepository {
        final List<AuditEvent> events = new ArrayList<>();
        @Override public void save(AuditEvent e) { events.add(e); }
    }

    static class StubOutboxEventRepository implements OutboxEventRepository {
        final List<OutboxEvent> events = new ArrayList<>();
        @Override public void save(OutboxEvent e) { events.add(e); }
        @Override public List<OutboxEvent> findPendingEvents(int limit) { return new ArrayList<>(); }
        @Override public void updateStatus(String id, String status, String err) {}
        @Override public void incrementAttempt(String id) {}
    }

    static class StubIdempotencyRecordRepository implements IdempotencyRecordRepository {
        final Map<String, IdempotencyRecord> records = new LinkedHashMap<>();
        @Override public void save(IdempotencyRecord r) { records.put(r.getScope() + ":" + r.getIdempotencyKey(), r); }
        @Override public Optional<IdempotencyRecord> findByKey(String scope, String key) {
            return Optional.ofNullable(records.get(scope + ":" + key));
        }
        @Override public void updateStatus(String id, String status, Integer rs, String rb) {}
        @Override public void deleteExpired(long ts) {}
    }

    static class StubNotificationPort implements IdentityNotificationPort {
        String lastVerificationToken;
        @Override public void sendEmailVerification(String email, String name, String token, String url) {
            lastVerificationToken = token;
        }
        @Override public void sendPasswordReset(String email, String name, String token, String url) {}
        @Override public void sendPasswordChanged(String email, String name) {}
        @Override public void sendAccountDisabled(String email, String name, String reason) {}
        @Override public void sendAccountCreated(String email, String name, String url) {}
    }
}