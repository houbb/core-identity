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

    // Stub IdentityInternalClient
    static class StubIdentityInternalClient implements IdentityInternalClient {
        boolean reachable = true;
        String status = "HEALTHY";

        @Override
        public Map<String, Object> getSystemInfo() {
            if (!reachable) return Map.of("error", "Connection refused");
            return Map.of(
                    "service", "core-identity-backend", "version", "0.1.0",
                    "apiVersion", "v1", "databaseType", "SQLite",
                    "databaseStatus", "CONNECTED", "flywayStatus", "MIGRATED",
                    "bootTime", System.currentTimeMillis()
            );
        }

        @Override
        public Map<String, Object> getHealthInfo() {
            if (!reachable) return Map.of("status", "UNAVAILABLE", "error", "timeout");
            return Map.of("status", status, "service", "core-identity-backend", "timestamp", System.currentTimeMillis());
        }

        @Override
        public boolean isReachable() { return reachable; }
    }

    @Nested
    @DisplayName("StubIdentityInternalClient")
    class StubClientTests {
        @Test
        @DisplayName("should return reachable by default")
        void shouldReturnReachable() {
            var client = new StubIdentityInternalClient();
            assertTrue(client.isReachable());
        }

        @Test
        @DisplayName("should return system info")
        void shouldReturnSystemInfo() {
            var client = new StubIdentityInternalClient();
            assertEquals("0.1.0", client.getSystemInfo().get("version"));
        }

        @Test
        @DisplayName("should return error when unreachable")
        void shouldReturnErrorWhenUnreachable() {
            var client = new StubIdentityInternalClient();
            client.reachable = false;
            assertTrue(client.getSystemInfo().containsKey("error"));
        }
    }
}