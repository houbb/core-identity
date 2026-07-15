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

        @Override public Map<String, Object> getSystemInfo() {
            if (!reachable) return Map.of("error", "Connection refused");
            return Map.of(
                    "service", "core-identity-backend", "version", "0.1.0",
                    "apiVersion", "v1", "databaseType", "SQLite",
                    "databaseStatus", "CONNECTED", "flywayStatus", "MIGRATED",
                    "bootTime", System.currentTimeMillis()
            );
        }
        @Override public Map<String, Object> getHealthInfo() {
            if (!reachable) return Map.of("status", "UNAVAILABLE", "error", "timeout");
            return Map.of("status", status, "service", "core-identity-backend", "timestamp", System.currentTimeMillis());
        }
        @Override public boolean isReachable() { return reachable; }

        // Admin auth stubs
        @Override public Map<String, Object> adminLogin(String email, String password) { return Map.of("userId", "u1"); }
        @Override public Map<String, Object> adminIntrospect(String token) { return Map.of("active", true); }
        @Override public void adminLogout(String token) {}

        // User management stubs
        @Override public Map<String, Object> listUsers(int page, int size, String status, String email) { return Map.of("users", java.util.List.of()); }
        @Override public Map<String, Object> createUser(Map<String, String> body) { return Map.of("userId", "new-user"); }
        @Override public Map<String, Object> getUserDetail(String userId) { return Map.of("id", userId); }
        @Override public Map<String, Object> disableUser(String userId, Map<String, String> body) { return Map.of("message", "disabled"); }
        @Override public Map<String, Object> enableUser(String userId) { return Map.of("message", "enabled"); }
        @Override public Map<String, Object> revokeSessions(String userId) { return Map.of("message", "revoked"); }
        @Override public Map<String, Object> resendVerification(String userId) { return Map.of("message", "sent"); }
        @Override public Map<String, Object> sendPasswordReset(String userId) { return Map.of("message", "sent"); }
        @Override public Map<String, Object> getLoginAttempts(String userId) { return Map.of("attempts", java.util.List.of()); }
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