package com.github.houbb.core.identity.api.publicapi.controller;

import com.github.houbb.core.identity.application.service.ApiKeyService;
import com.github.houbb.core.identity.application.service.ServiceAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/identity")
public class ApiKeyController {
    private final ApiKeyService apiKeyService;
    private final ServiceAccountService saService;

    public ApiKeyController(ApiKeyService apiKeyService, ServiceAccountService saService) { this.apiKeyService=apiKeyService; this.saService=saService; }

    @PostMapping("/developer/api-keys")
    public ResponseEntity<?> createKey(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String ownerType = (String) body.getOrDefault("ownerType", "USER");
        String ownerId = (String) body.get("ownerId");
        String orgId = (String) body.get("organizationId");
        Long expiresAt = body.get("expiresAt") instanceof Number n ? n.longValue() : null;
        var result = apiKeyService.createKey(name, ownerType, ownerId, orgId, null, null, expiresAt);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("keyId", result.key().getId()); resp.put("keyPrefix", result.key().getKeyPrefix());
        resp.put("rawKey", result.rawKey()); resp.put("warning", "Save this key — it will not be shown again.");
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/developer/api-keys")
    public ResponseEntity<?> listKeys(@RequestParam String userId) { return ResponseEntity.ok(apiKeyService.getUserKeys(userId)); }

    @DeleteMapping("/developer/api-keys/{keyId}")
    public ResponseEntity<?> revokeKey(@PathVariable String keyId) { apiKeyService.revokeKey(keyId); return ResponseEntity.ok(Map.of("status","revoked")); }

    @PostMapping("/developer/service-accounts")
    public ResponseEntity<?> createServiceAccount(@RequestBody Map<String, Object> body) {
        String orgId = (String) body.get("organizationId");
        String name = (String) body.get("name");
        String desc = (String) body.get("description");
        String createdBy = (String) body.get("createdBy");
        return ResponseEntity.ok(saService.createAccount(orgId, name, desc, createdBy));
    }

    @GetMapping("/developer/service-accounts")
    public ResponseEntity<?> listServiceAccounts(@RequestParam String organizationId) { return ResponseEntity.ok(saService.listOrgAccounts(organizationId)); }
}