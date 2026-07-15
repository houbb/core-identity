package com.github.houbb.core.identity.api.publicapi.controller;

import com.github.houbb.core.identity.application.domain.Scope;
import com.github.houbb.core.identity.application.service.ScopeCatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Public API for scope and audience discovery.
 */
@RestController
@RequestMapping("/api/v1/identity")
public class ScopeController {

    private final ScopeCatalogService scopeCatalogService;

    public ScopeController(ScopeCatalogService scopeCatalogService) {
        this.scopeCatalogService = scopeCatalogService;
    }

    @GetMapping("/scopes")
    public ResponseEntity<List<Scope>> listScopes(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String audienceCode,
            @RequestParam(required = false) String riskLevel) {
        return ResponseEntity.ok(scopeCatalogService.getAssignableScopes(service, audienceCode, riskLevel));
    }

    @GetMapping("/scopes/{scopeId}")
    public ResponseEntity<Map<String, Object>> getScope(@PathVariable String scopeId) {
        List<Scope> scopes = scopeCatalogService.getScopesByIds(List.of(scopeId));
        if (scopes.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Scope scope = scopes.get(0);
        return ResponseEntity.ok(Map.of(
                "scope", scope,
                "permissions", scopeCatalogService.getScopePermissions(scopeId)
        ));
    }
}