package com.github.houbb.core.identity.api.internal.controller;

import com.github.houbb.core.identity.application.service.PermissionCatalogService;
import com.github.houbb.core.identity.application.service.PermissionCatalogService.PermissionManifestEntry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Internal API for permission catalog synchronization.
 */
@RestController
@RequestMapping("/internal/v1/identity")
public class InternalPermissionController {

    private final PermissionCatalogService catalogService;

    public InternalPermissionController(PermissionCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @PutMapping("/permission-sources/{service}")
    public ResponseEntity<Map<String, Object>> syncPermissions(
            @PathVariable String service,
            @RequestBody Map<String, Object> body) {

        String version = (String) body.getOrDefault("version", "1");
        String syncedBy = (String) body.getOrDefault("syncedBy", "system");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawPermissions = (List<Map<String, Object>>) body.get("permissions");

        List<PermissionManifestEntry> entries = new ArrayList<>();
        if (rawPermissions != null) {
            for (Map<String, Object> p : rawPermissions) {
                entries.add(new PermissionManifestEntry(
                        (String) p.get("code"),
                        (String) p.get("name"),
                        (String) p.get("resource"),
                        (String) p.get("action"),
                        (String) p.getOrDefault("riskLevel", "LOW").toString(),
                        (String) p.get("description")
                ));
            }
        }

        catalogService.syncPermissions(service, version, entries, syncedBy);

        return ResponseEntity.ok(Map.of(
                "status", "synced",
                "service", service,
                "version", version,
                "count", entries.size()
        ));
    }
}