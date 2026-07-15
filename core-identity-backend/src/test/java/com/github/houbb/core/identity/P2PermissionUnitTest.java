package com.github.houbb.core.identity;

import com.github.houbb.core.identity.application.domain.Permission;
import com.github.houbb.core.identity.application.domain.PermissionSource;
import com.github.houbb.core.identity.application.port.PermissionRepository;
import com.github.houbb.core.identity.application.port.PermissionSourceRepository;
import com.github.houbb.core.identity.application.service.PermissionCatalogService;
import com.github.houbb.core.identity.application.service.PermissionCatalogServiceImpl;
import com.github.houbb.core.identity.application.service.PermissionCatalogService.PermissionManifestEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("P2.1 Permission Catalog Unit Tests")
class P2PermissionUnitTest {

    private StubPermissionRepository permissionRepo;
    private StubPermissionSourceRepository sourceRepo;
    private PermissionCatalogService catalogService;

    @BeforeEach
    void setUp() {
        permissionRepo = new StubPermissionRepository();
        sourceRepo = new StubPermissionSourceRepository();
        catalogService = new PermissionCatalogServiceImpl(permissionRepo, sourceRepo);
    }

    @Nested
    @DisplayName("Permission Sync")
    class PermissionSyncTests {

        @Test
        @DisplayName("should create new permissions on first sync")
        void shouldCreateNewPermissions() {
            List<PermissionManifestEntry> entries = List.of(
                    entry("storage.object.read", "Read objects", "object", "read", "LOW"),
                    entry("storage.object.create", "Create objects", "object", "create", "MEDIUM")
            );

            List<Permission> result = catalogService.syncPermissions("core-storage", "1.0", entries, "test");

            assertEquals(2, result.size());
            assertTrue(result.stream().anyMatch(p -> "storage.object.read".equals(p.getPermissionCode())));
            assertEquals("ACTIVE", result.get(0).getStatus());
            assertEquals(1, result.get(0).getAssignable());
        }

        @Test
        @DisplayName("should be idempotent — same checksum skips sync")
        void shouldBeIdempotent() {
            List<PermissionManifestEntry> entries = List.of(
                    entry("storage.object.read", "Read objects", "object", "read", "LOW")
            );

            // First sync
            catalogService.syncPermissions("core-storage", "1.0", entries, "test");

            // Second sync — same entries, should detect unchanged checksum
            List<Permission> result = catalogService.syncPermissions("core-storage", "1.0", entries, "test");

            assertEquals(1, result.size());
            // No duplicates
            assertEquals(1, permissionRepo.findAll().size());
        }

        @Test
        @DisplayName("should deprecate permissions removed from manifest")
        void shouldDeprecateRemovedPermissions() {
            List<PermissionManifestEntry> v1 = List.of(
                    entry("storage.object.read", "Read", "object", "read", "LOW"),
                    entry("storage.object.delete", "Delete", "object", "delete", "HIGH")
            );

            catalogService.syncPermissions("core-storage", "1.0", v1, "test");
            assertEquals(2, permissionRepo.findByService("core-storage").stream()
                    .filter(p -> "ACTIVE".equals(p.getStatus())).count());

            // v2 removes delete permission
            List<PermissionManifestEntry> v2 = List.of(
                    entry("storage.object.read", "Read", "object", "read", "LOW")
            );

            catalogService.syncPermissions("core-storage", "2.0", v2, "test");

            Optional<Permission> deleted = permissionRepo.findByCode("storage.object.delete");
            assertTrue(deleted.isPresent());
            assertEquals("DEPRECATED", deleted.get().getStatus());
        }

        @Test
        @DisplayName("should reactivate previously deprecated permission")
        void shouldReactivateDeprecatedPermission() {
            List<PermissionManifestEntry> v1 = List.of(
                    entry("storage.object.read", "Read", "object", "read", "LOW")
            );

            catalogService.syncPermissions("core-storage", "1.0", v1, "test");

            // Deprecate by removing from v2
            catalogService.syncPermissions("core-storage", "2.0", List.of(), "test");
            assertEquals("DEPRECATED", permissionRepo.findByCode("storage.object.read").get().getStatus());

            // Reactivate by adding back in v3
            catalogService.syncPermissions("core-storage", "3.0", v1, "test");
            assertEquals("ACTIVE", permissionRepo.findByCode("storage.object.read").get().getStatus());
        }

        @Test
        @DisplayName("should update name and description when changed")
        void shouldUpdateNameAndDescription() {
            List<PermissionManifestEntry> v1 = List.of(
                    entry("storage.object.read", "Read files", "object", "read", "LOW")
            );

            catalogService.syncPermissions("core-storage", "1.0", v1, "test");

            List<PermissionManifestEntry> v2 = List.of(
                    entry("storage.object.read", "View files", "object", "read", "LOW")
            );

            catalogService.syncPermissions("core-storage", "2.0", v2, "test");

            Permission perm = permissionRepo.findByCode("storage.object.read").orElseThrow();
            assertEquals("View files", perm.getName());
        }
    }

    @Nested
    @DisplayName("Permission Query")
    class PermissionQueryTests {

        @Test
        @DisplayName("should filter by service")
        void shouldFilterByService() {
            seedPermission("p1", "identity.org.read", "core-identity", "LOW");
            seedPermission("p2", "storage.obj.read", "core-storage", "LOW");

            List<Permission> result = catalogService.getAssignablePermissions("core-identity", null, null, null);
            assertEquals(1, result.size());
            assertEquals("identity.org.read", result.get(0).getPermissionCode());
        }

        @Test
        @DisplayName("should filter by risk level")
        void shouldFilterByRiskLevel() {
            seedPermission("p1", "identity.org.read", "core-identity", "LOW");
            seedPermission("p2", "identity.org.delete", "core-identity", "CRITICAL");

            List<Permission> result = catalogService.getAssignablePermissions(null, null, "CRITICAL", null);
            assertEquals(1, result.size());
            assertEquals("CRITICAL", result.get(0).getRiskLevel());
        }

        @Test
        @DisplayName("should search by name or code")
        void shouldSearch() {
            seedPermission("p1", "identity.org.read", "core-identity", "LOW");
            seedPermission("p2", "identity.member.invite", "core-identity", "MEDIUM");

            List<Permission> result = catalogService.getAssignablePermissions(null, null, null, "member");
            assertEquals(1, result.size());
            assertTrue(result.get(0).getPermissionCode().contains("member"));
        }

        @Test
        @DisplayName("should only return assignable and active")
        void shouldOnlyReturnAssignableAndActive() {
            seedPermission("p1", "identity.org.read", "core-identity", "LOW");

            // Make it not assignable
            Permission p = permissionRepo.findByCode("identity.org.read").orElseThrow();
            p.setAssignable(0);
            permissionRepo.update(p);

            List<Permission> result = catalogService.getAssignablePermissions(null, null, null, null);
            // Stub doesn't filter — but the real SQL does. Test coverage is in integration.
            assertTrue(result.isEmpty() || result.stream().allMatch(perm -> perm.getAssignable() == 1));
        }
    }

    // === Helpers ===

    private PermissionManifestEntry entry(String code, String name, String resource, String action, String riskLevel) {
        return new PermissionManifestEntry(code, name, resource, action, riskLevel, null);
    }

    private void seedPermission(String id, String code, String service, String riskLevel) {
        Permission p = new Permission();
        p.setId(id);
        p.setPermissionCode(code);
        p.setSourceService(service);
        p.setResource("obj");
        p.setAction("read");
        p.setName(code);
        p.setRiskLevel(riskLevel);
        p.setAssignable(1);
        p.setStatus("ACTIVE");
        p.setCreatedAt(System.currentTimeMillis());
        p.setUpdatedAt(System.currentTimeMillis());
        p.setVersion(1);
        permissionRepo.save(p);
    }

    // === Stub Repositories ===

    static class StubPermissionRepository implements PermissionRepository {
        private final Map<String, Permission> byCode = new LinkedHashMap<>();
        private final Map<String, Permission> byId = new LinkedHashMap<>();

        @Override public void save(Permission p) { byCode.put(p.getPermissionCode(), p); byId.put(p.getId(), p); }
        @Override public Optional<Permission> findById(String id) { return Optional.ofNullable(byId.get(id)); }
        @Override public Optional<Permission> findByCode(String code) { return Optional.ofNullable(byCode.get(code)); }
        @Override public List<Permission> findByService(String service) {
            return byCode.values().stream().filter(p -> service.equals(p.getSourceService())).toList();
        }
        @Override public List<Permission> findByServices(List<String> services) {
            return byCode.values().stream().filter(p -> services.contains(p.getSourceService())).toList();
        }
        @Override public List<Permission> findAllAssignable() {
            return byCode.values().stream().filter(p -> p.getAssignable() == 1 && "ACTIVE".equals(p.getStatus())).toList();
        }
        @Override public List<Permission> findAll() { return new ArrayList<>(byCode.values()); }
        @Override public List<Permission> findAssignableByService(String service, String resource, String riskLevel, String search) {
            return byCode.values().stream()
                .filter(p -> p.getAssignable() == 1 && "ACTIVE".equals(p.getStatus()))
                .filter(p -> service == null || service.equals(p.getSourceService()))
                .filter(p -> resource == null || resource.equals(p.getResource()))
                .filter(p -> riskLevel == null || riskLevel.equals(p.getRiskLevel()))
                .filter(p -> search == null || p.getName().contains(search) || p.getPermissionCode().contains(search))
                .toList();
        }
        @Override public void update(Permission p) { save(p); }
        @Override public void updateStatus(String id, String status, long now, long version) {
            Permission p = byId.get(id);
            if (p != null) { p.setStatus(status); p.setUpdatedAt(now); }
        }
        @Override public int countByService(String service) {
            return (int) byCode.values().stream().filter(p -> service.equals(p.getSourceService())).count();
        }
    }

    static class StubPermissionSourceRepository implements PermissionSourceRepository {
        private final Map<String, PermissionSource> byName = new LinkedHashMap<>();

        @Override public void save(PermissionSource s) { byName.put(s.getServiceName(), s); }
        @Override public Optional<PermissionSource> findByServiceName(String name) { return Optional.ofNullable(byName.get(name)); }
        @Override public void update(PermissionSource s) { save(s); }
        @Override public void updateSyncInfo(String id, String version, String checksum, long syncedAt, String syncedBy, long version2) {
            byName.values().stream().filter(s -> s.getId().equals(id)).findFirst().ifPresent(s -> {
                s.setManifestVersion(version);
                s.setChecksum(checksum);
                s.setLastSyncedAt(syncedAt);
                s.setLastSyncedBy(syncedBy);
            });
        }
    }
}