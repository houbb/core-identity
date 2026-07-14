package com.github.houbb.core.identity;

import com.github.houbb.core.identity.application.command.AuditCommand;
import com.github.houbb.core.identity.application.command.CreateServiceTokenCommand;
import com.github.houbb.core.identity.application.command.IdempotencyCommand;
import com.github.houbb.core.identity.application.command.OutboxCommand;
import com.github.houbb.core.identity.application.domain.AuditEvent;
import com.github.houbb.core.identity.application.domain.IdempotencyRecord;
import com.github.houbb.core.identity.application.domain.InternalClient;
import com.github.houbb.core.identity.application.domain.OutboxEvent;
import com.github.houbb.core.identity.application.port.*;
import com.github.houbb.core.identity.application.service.*;
import com.github.houbb.core.identity.application.service.InternalTokenServiceImpl.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for core-identity-backend application services.
 */
@DisplayName("Core Identity Backend Unit Tests")
class CoreIdentityBackendUnitTest {

    // In-memory stubs
    private final StubInternalClientRepository clientRepo = new StubInternalClientRepository();
    private final StubAuditEventRepository auditRepo = new StubAuditEventRepository();
    private final StubOutboxEventRepository outboxRepo = new StubOutboxEventRepository();
    private final StubIdempotencyRecordRepository idempotencyRepo = new StubIdempotencyRecordRepository();

    private SystemInfoService systemInfoService;
    private InternalTokenService internalTokenService;
    private AuditService auditService;
    private OutboxService outboxService;
    private IdempotencyService idempotencyService;

    @BeforeEach
    void setUp() {
        clientRepo.reset();
        auditRepo.reset();
        outboxRepo.reset();
        idempotencyRepo.reset();

        systemInfoService = new SystemInfoServiceImpl("0.1.0", "v1", "Test Identity", "COMMUNITY");

        // Use a 256-bit key (32 chars) for HS256
        String signingKey = "test-signing-key-32-bytes-ok!!";
        internalTokenService = new InternalTokenServiceImpl(clientRepo, signingKey, "test-issuer", 600);

        auditService = new AuditServiceImpl(auditRepo);
        outboxService = new OutboxServiceImpl(outboxRepo);
        idempotencyService = new IdempotencyServiceImpl(idempotencyRepo, 24);

        // Seed internal client
        InternalClient client = new InternalClient();
        client.setId("c1");
        client.setClientId("admin-backend");
        client.setClientSecretHash(InternalTokenServiceImpl.hashSecret("test-secret"));
        client.setDisplayName("Test Client");
        client.setClientType("SERVICE");
        client.setScopes(List.of("identity.system.read", "identity.audit.write"));
        client.setStatus("ACTIVE");
        client.setCreatedAt(System.currentTimeMillis());
        client.setUpdatedAt(System.currentTimeMillis());
        client.setVersion(1);
        clientRepo.save(client);
    }

    // === SystemInfoService Tests ===

    @Nested
    @DisplayName("SystemInfoService")
    class SystemInfoServiceTests {

        @Test
        @DisplayName("should return version")
        void shouldReturnVersion() {
            assertEquals("0.1.0", systemInfoService.getVersion());
        }

        @Test
        @DisplayName("should return api version")
        void shouldReturnApiVersion() {
            assertEquals("v1", systemInfoService.getApiVersion());
        }

        @Test
        @DisplayName("should return running status")
        void shouldReturnRunningStatus() {
            assertEquals("RUNNING", systemInfoService.getStatus());
        }

        @Test
        @DisplayName("should return non-empty capabilities")
        void shouldReturnNonEmptyCapabilities() {
            assertNotNull(systemInfoService.getCapabilities());
            assertTrue(systemInfoService.getCapabilities().length > 0);
        }

        @Test
        @DisplayName("should return expected capabilities")
        void shouldReturnExpectedCapabilities() {
            String[] capabilities = systemInfoService.getCapabilities();
            assertTrue(List.of(capabilities).contains("SYSTEM_META"));
            assertTrue(List.of(capabilities).contains("INTERNAL_SERVICE_AUTH"));
            assertTrue(List.of(capabilities).contains("AUDIT_FOUNDATION"));
            assertTrue(List.of(capabilities).contains("OUTBOX_FOUNDATION"));
        }
    }

    // === InternalTokenService Tests ===

    @Nested
    @DisplayName("InternalTokenService")
    class InternalTokenServiceTests {

        @Test
        @DisplayName("should issue token with valid credentials")
        void shouldIssueTokenWithValidCredentials() {
            String token = internalTokenService.issueToken("admin-backend", "test-secret");
            assertNotNull(token);
            assertFalse(token.isEmpty());
        }

        @Test
        @DisplayName("should reject invalid secret")
        void shouldRejectInvalidSecret() {
            assertThrows(AuthenticationException.class, () ->
                    internalTokenService.issueToken("admin-backend", "wrong-secret"));
        }

        @Test
        @DisplayName("should reject unknown client")
        void shouldRejectUnknownClient() {
            assertThrows(AuthenticationException.class, () ->
                    internalTokenService.issueToken("unknown", "test-secret"));
        }

        @Test
        @DisplayName("should validate issued token")
        void shouldValidateIssuedToken() {
            String token = internalTokenService.issueToken("admin-backend", "test-secret");
            String subject = internalTokenService.validateToken(token);
            assertEquals("admin-backend", subject);
        }

        @Test
        @DisplayName("should reject invalid token")
        void shouldRejectInvalidToken() {
            assertThrows(AuthenticationException.class, () ->
                    internalTokenService.validateToken("invalid-token"));
        }

        @Test
        @DisplayName("should check scope correctly")
        void shouldCheckScopeCorrectly() {
            String token = internalTokenService.issueToken("admin-backend", "test-secret");
            assertTrue(internalTokenService.hasScope(token, "identity.system.read"));
            assertTrue(internalTokenService.hasScope(token, "identity.audit.write"));
            assertFalse(internalTokenService.hasScope(token, "identity.admin.manage"));
        }

        @Test
        @DisplayName("should reject disabled client")
        void shouldRejectDisabledClient() {
            // Create a disabled client
            InternalClient disabled = new InternalClient();
            disabled.setId("c2");
            disabled.setClientId("disabled-client");
            disabled.setClientSecretHash(InternalTokenServiceImpl.hashSecret("secret"));
            disabled.setDisplayName("Disabled");
            disabled.setClientType("SERVICE");
            disabled.setStatus("DISABLED");
            clientRepo.save(disabled);

            assertThrows(AuthenticationException.class, () ->
                    internalTokenService.issueToken("disabled-client", "secret"));
        }

        @Test
        @DisplayName("should reject expired client")
        void shouldRejectExpiredClient() {
            InternalClient expired = new InternalClient();
            expired.setId("c3");
            expired.setClientId("expired-client");
            expired.setClientSecretHash(InternalTokenServiceImpl.hashSecret("secret"));
            expired.setDisplayName("Expired");
            expired.setClientType("SERVICE");
            expired.setStatus("ACTIVE");
            expired.setExpiresAt(System.currentTimeMillis() - 10000);
            clientRepo.save(expired);

            assertThrows(AuthenticationException.class, () ->
                    internalTokenService.issueToken("expired-client", "secret"));
        }
    }

    // === AuditService Tests ===

    @Nested
    @DisplayName("AuditService")
    class AuditServiceTests {

        @Test
        @DisplayName("should record audit event")
        void shouldRecordAuditEvent() {
            AuditCommand cmd = new AuditCommand();
            cmd.setEventType("TEST_EVENT");
            cmd.setActorType("SYSTEM");
            cmd.setActorId("test-system");
            cmd.setAction("TEST");
            cmd.setResult("SUCCESS");

            String eventId = auditService.record(cmd);
            assertNotNull(eventId);
            assertFalse(eventId.isEmpty());

            List<AuditEvent> events = auditRepo.getSavedEvents();
            assertEquals(1, events.size());
            assertEquals("TEST_EVENT", events.get(0).getEventType());
            assertEquals("SYSTEM", events.get(0).getActorType());
        }
    }

    // === OutboxService Tests ===

    @Nested
    @DisplayName("OutboxService")
    class OutboxServiceTests {

        @Test
        @DisplayName("should write outbox event")
        void shouldWriteOutboxEvent() {
            OutboxCommand cmd = new OutboxCommand();
            cmd.setEventType("test.event");
            cmd.setAggregateType("Test");
            cmd.setAggregateId("t1");
            cmd.setPayloadJson("{\"key\":\"value\"}");

            String eventId = outboxService.write(cmd);
            assertNotNull(eventId);

            List<OutboxEvent> events = outboxRepo.getSavedEvents();
            assertEquals(1, events.size());
            assertEquals("test.event", events.get(0).getEventType());
            assertEquals("PENDING", events.get(0).getStatus());
        }
    }

    // === IdempotencyService Tests ===

    @Nested
    @DisplayName("IdempotencyService")
    class IdempotencyServiceTests {

        @Test
        @DisplayName("should allow first request")
        void shouldAllowFirstRequest() {
            IdempotencyCommand cmd = new IdempotencyCommand();
            cmd.setScope("test");
            cmd.setIdempotencyKey("key-1");

            assertTrue(idempotencyService.checkOrCreate(cmd));
        }

        @Test
        @DisplayName("should detect duplicate request (conflict)")
        void shouldDetectDuplicateRequest() {
            IdempotencyCommand cmd = new IdempotencyCommand();
            cmd.setScope("test");
            cmd.setIdempotencyKey("key-2");

            assertTrue(idempotencyService.checkOrCreate(cmd));
            // Second call with same key should return false (still PROCESSING)
            assertFalse(idempotencyService.checkOrCreate(cmd));
        }

        @Test
        @DisplayName("should mark succeeded")
        void shouldMarkSucceeded() {
            IdempotencyCommand cmd = new IdempotencyCommand();
            cmd.setScope("test");
            cmd.setIdempotencyKey("key-3");

            idempotencyService.checkOrCreate(cmd);
            idempotencyService.markSucceeded(cmd, 200, "ok");

            IdempotencyRecord record = idempotencyRepo.findByKey("test", "key-3").orElseThrow();
            assertEquals("SUCCEEDED", record.getStatus());
        }

        @Test
        @DisplayName("should mark failed")
        void shouldMarkFailed() {
            IdempotencyCommand cmd = new IdempotencyCommand();
            cmd.setScope("test");
            cmd.setIdempotencyKey("key-4");

            idempotencyService.checkOrCreate(cmd);
            idempotencyService.markFailed(cmd, 500, "error");

            IdempotencyRecord record = idempotencyRepo.findByKey("test", "key-4").orElseThrow();
            assertEquals("FAILED", record.getStatus());
        }
    }

    // === Hash Secret Tests ===

    @Nested
    @DisplayName("Secret Hashing")
    class SecretHashTests {

        @Test
        @DisplayName("should produce consistent hash")
        void shouldProduceConsistentHash() {
            String hash1 = InternalTokenServiceImpl.hashSecret("my-secret");
            String hash2 = InternalTokenServiceImpl.hashSecret("my-secret");
            assertEquals(hash1, hash2);
        }

        @Test
        @DisplayName("should produce different hashes for different secrets")
        void shouldProduceDifferentHashes() {
            String hash1 = InternalTokenServiceImpl.hashSecret("secret-a");
            String hash2 = InternalTokenServiceImpl.hashSecret("secret-b");
            assertNotEquals(hash1, hash2);
        }
    }

    // === Stub repositories ===

    static class StubInternalClientRepository implements InternalClientRepository {
        private final java.util.Map<String, InternalClient> clients = new java.util.HashMap<>();

        void reset() { clients.clear(); }

        @Override
        public void save(InternalClient client) { clients.put(client.getClientId(), client); }

        @Override
        public Optional<InternalClient> findByClientId(String clientId) {
            return Optional.ofNullable(clients.get(clientId));
        }

        @Override
        public void updateLastUsedAt(String clientId, long timestamp) {
            InternalClient c = clients.get(clientId);
            if (c != null) c.setLastUsedAt(timestamp);
        }
    }

    static class StubAuditEventRepository implements AuditEventRepository {
        private final java.util.List<AuditEvent> events = new java.util.ArrayList<>();

        void reset() { events.clear(); }

        @Override
        public void save(AuditEvent event) { events.add(event); }

        List<AuditEvent> getSavedEvents() { return List.copyOf(events); }
    }

    static class StubOutboxEventRepository implements OutboxEventRepository {
        private final java.util.List<OutboxEvent> events = new java.util.ArrayList<>();

        void reset() { events.clear(); }

        @Override
        public void save(OutboxEvent event) { events.add(event); }

        @Override
        public List<OutboxEvent> findPendingEvents(int limit) { return events; }

        @Override
        public void updateStatus(String eventId, String status, String lastError) {}

        @Override
        public void incrementAttempt(String eventId) {}

        List<OutboxEvent> getSavedEvents() { return List.copyOf(events); }
    }

    static class StubIdempotencyRecordRepository implements IdempotencyRecordRepository {
        private final java.util.Map<String, IdempotencyRecord> records = new java.util.HashMap<>();

        void reset() { records.clear(); }

        @Override
        public void save(IdempotencyRecord record) {
            records.put(record.getScope() + ":" + record.getIdempotencyKey(), record);
        }

        @Override
        public Optional<IdempotencyRecord> findByKey(String scope, String idempotencyKey) {
            return Optional.ofNullable(records.get(scope + ":" + idempotencyKey));
        }

        @Override
        public void updateStatus(String id, String status, Integer responseStatus, String responseBody) {
            records.values().stream()
                    .filter(r -> r.getId().equals(id))
                    .findFirst()
                    .ifPresent(r -> {
                        r.setStatus(status);
                        r.setResponseStatus(responseStatus);
                        r.setResponseBody(responseBody);
                    });
        }

        @Override
        public void deleteExpired(long beforeTimestamp) {}
    }
}