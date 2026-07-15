package com.github.houbb.core.identity.api.publicapi.controller;

import com.github.houbb.core.identity.application.domain.Permission;
import com.github.houbb.core.identity.application.service.PermissionCatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Public API for permission catalog browsing.
 */
@RestController
@RequestMapping("/api/v1/identity")
public class PermissionController {

    private final PermissionCatalogService catalogService;

    public PermissionController(PermissionCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/permissions")
    public ResponseEntity<Map<String, Object>> getPermissions(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String resource,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String search) {

        List<Permission> permissions = catalogService.getAssignablePermissions(service, resource, riskLevel, search);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Permission p : permissions) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", p.getId());
            item.put("code", p.getPermissionCode());
            item.put("name", p.getName());
            item.put("description", p.getDescription());
            item.put("service", p.getSourceService());
            item.put("resource", p.getResource());
            item.put("action", p.getAction());
            item.put("riskLevel", p.getRiskLevel());
            result.add(item);
        }

        return ResponseEntity.ok(Map.of(
                "permissions", result,
                "total", result.size()
        ));
    }
}