package com.github.houbb.core.identity.api.internal.controller;

import com.github.houbb.core.identity.application.service.ScopeCatalogService;
import com.github.houbb.core.identity.application.service.ScopeCatalogService.ScopeManifestEntry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Internal API for scope and audience catalog synchronization.
 */
@RestController
@RequestMapping("/internal/v1/identity")
public class InternalScopeController {

    private final ScopeCatalogService scopeCatalogService;

    public InternalScopeController(ScopeCatalogService scopeCatalogService) {
        this.scopeCatalogService = scopeCatalogService;
    }

    @PutMapping("/scope-sources/{service}")
    public ResponseEntity<Map<String, Object>> syncScopes(
            @PathVariable String service,
            @RequestBody Map<String, Object> body) {

        String version = body.getOrDefault("version", "1").toString();
        String syncedBy = (String) body.getOrDefault("syncedBy", "system");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawScopes = (List<Map<String, Object>>) body.get("scopes");

        List<ScopeManifestEntry> entries = new ArrayList<>();
        if (rawScopes != null) {
            for (Map<String, Object> s : rawScopes) {
                entries.add(new ScopeManifestEntry(
                        (String) s.get("code"),
                        (String) s.get("name"),
                        (String) s.get("audienceCode"),
                        (String) s.getOrDefault("riskLevel", "LOW"),
                        (String) s.get("consentDisplay"),
                        (String) s.get("description")
                ));
            }
        }

        scopeCatalogService.syncScopes(service, version, entries, syncedBy);

        return ResponseEntity.ok(Map.of(
                "status", "synced",
                "service", service,
                "version", version,
                "count", entries.size()
        ));
    }

    @PutMapping("/scope-sources/{scopeCode}/permissions")
    public ResponseEntity<Map<String, Object>> syncScopePermissions(
            @PathVariable String scopeCode,
            @RequestBody Map<String, Object> body) {

        String scopeId = (String) body.get("scopeId");
        String syncedBy = (String) body.getOrDefault("syncedBy", "system");

        @SuppressWarnings("unchecked")
        List<String> permissionIds = (List<String>) body.get("permissionIds");

        scopeCatalogService.syncScopePermissions(scopeId,
                permissionIds != null ? permissionIds : List.of(),
                syncedBy);

        return ResponseEntity.ok(Map.of(
                "status", "synced",
                "scopeCode", scopeCode,
                "permissionCount", permissionIds != null ? permissionIds.size() : 0
        ));
    }
}