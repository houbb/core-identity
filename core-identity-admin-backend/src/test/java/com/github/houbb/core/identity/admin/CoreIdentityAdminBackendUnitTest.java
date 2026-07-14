package com.github.houbb.core.identity.admin;

import com.github.houbb.core.identity.admin.application.service.BackendHealthAggregationService;
import com.github.houbb.core.identity.admin.application.service.ContractCompatibilityService;
import com.github.houbb.core.identity.admin.application.service.SystemOverviewService;
import com.github.houbb.core.identity.admin.infrastructure.identityclient.IdentityInternalClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for core-identity-admin-backend.
 */
@DisplayName("Core Identity Admin Backend Unit Tests")
class CoreIdentityAdminBackendUnitTest {

    // Stub IdentityInternalClient for testing
    static class StubIdentityInternalClient implements IdentityInternalClient {
        private boolean reachable = true;
        private String status = "HEALTHY";

        void setReachable(boolean reachable) { this.reachable = reachable; }
        void setStatus(String status) { this.status = status; }

        @Override
        public Map<String, Object> getSystemInfo() {
            if (!reachable) {
                return Map.of("error", "Connection refused");
            }
            return Map.of(
                    "service", "core-identity-backend",
                    "version", "0.1.0",
                    "apiVersion", "v1",
                    "databaseType", "SQLite",
                    "databaseStatus", "CONNECTED",
                    "flywayStatus", "MIGRATED",
                    "bootTime", System.currentTimeMillis()
            );
        }

        @Override
        public Map<String, Object> getHealthInfo() {
            if (!reachable) {
                return Map.of("status", "UNAVAILABLE", "error", "timeout");
            }
            return Map.of(
                    "status", status,
                    "service", "core-identity-backend",
                    "timestamp", System.currentTimeMillis()
            );
        }

        @Override
        public boolean isReachable() {
            return reachable;
        }
    }

    @Nested
    @DisplayName("SystemOverviewService")
    class SystemOverviewServiceTest {

        @Test
        @DisplayName("should return overview with identity backend reachable")
        void shouldReturnOverviewWithIdentityReachable() {
            StubIdentityInternalClient client = new StubIdentityInternalClient();
            SystemOverviewService service = new SystemOverviewServiceImpl(client);

            Map<String, Object> overview = service.getOverview();
            assertNotNull(overview);
            assertEquals("HEALTHY", overview.get("overallStatus"));

            @SuppressWarnings("unchecked")
            Map<String, Object> identityBackend = (Map<String, Object>) overview.get("identityBackend");
            assertNotNull(identityBackend);
            assertTrue((Boolean) identityBackend.get("reachable"));
        }

        @Test
        @DisplayName("should return degraded when identity backend unreachable")
        void shouldReturnDegradedWhenIdentityUnreachable() {
            StubIdentityInternalClient client = new StubIdentityInternalClient();
            client.setReachable(false);
            SystemOverviewService service = new SystemOverviewServiceImpl(client);

            Map<String, Object> overview = service.getOverview();
            assertEquals("DEGRADED", overview.get("overallStatus"));
        }
    }

    @Nested
    @DisplayName("BackendHealthAggregationService")
    class BackendHealthAggregationServiceTest {

        @Test
        @DisplayName("should aggregate health healthy")
        void shouldAggregateHealthHealthy() {
            StubIdentityInternalClient client = new StubIdentityInternalClient();
            BackendHealthAggregationService service = new BackendHealthAggregationServiceImpl(client);

            assertEquals("HEALTHY", service.getAggregatedHealth());

            Map<String, Object> health = service.getDetailedHealth();
            assertNotNull(health);
            @SuppressWarnings("unchecked")
            Map<String, Object> admin = (Map<String, Object>) health.get("adminBackend");
            assertEquals("HEALTHY", admin.get("status"));
        }

        @Test
        @DisplayName("should aggregate health degraded when identity unavailable")
        void shouldAggregateHealthDegraded() {
            StubIdentityInternalClient client = new StubIdentityInternalClient();
            client.setReachable(false);
            BackendHealthAggregationService service = new BackendHealthAggregationServiceImpl(client);

            assertEquals("DEGRADED", service.getAggregatedHealth());
        }
    }

    @Nested
    @DisplayName("ContractCompatibilityService")
    class ContractCompatibilityServiceTest {

        @Test
        @DisplayName("should check compatible contracts")
        void shouldCheckCompatibleContracts() {
            StubIdentityInternalClient client = new StubIdentityInternalClient();
            ContractCompatibilityService service = new ContractCompatibilityServiceImpl(client);

            assertEquals("COMPATIBLE", service.checkCompatibility());
        }

        @Test
        @DisplayName("should check unavailable when identity unreachable")
        void shouldCheckUnavailableWhenIdentityUnreachable() {
            StubIdentityInternalClient client = new StubIdentityInternalClient();
            client.setReachable(false);
            ContractCompatibilityService service = new ContractCompatibilityServiceImpl(client);

            assertEquals("UNAVAILABLE", service.checkCompatibility());
        }

        @Test
        @DisplayName("should return contract versions")
        void shouldReturnContractVersions() {
            StubIdentityInternalClient client = new StubIdentityInternalClient();
            ContractCompatibilityService service = new ContractCompatibilityServiceImpl(client);

            Map<String, Object> versions = service.getContractVersions();
            assertEquals("COMPATIBLE", versions.get("compatibility"));
            assertEquals("v1", versions.get("adminApiVersion"));
        }
    }
}