package com.github.houbb.core.identity.api.publicapi.controller;

import com.github.houbb.core.identity.application.domain.OAuthClient;
import com.github.houbb.core.identity.application.service.OAuthClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/identity/developer/clients")
public class DeveloperClientController {
    private final OAuthClientService clientService;

    public DeveloperClientController(OAuthClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String description = (String) body.get("description");
        String clientType = (String) body.getOrDefault("clientType", "CONFIDENTIAL");
        String userId = (String) body.get("userId");

        var result = clientService.createClient("USER", userId, name, description, clientType, null, userId);
        Map<String, Object> resp = new java.util.LinkedHashMap<>();
        resp.put("clientId", result.client().getClientId());
        resp.put("internalId", result.client().getId());
        resp.put("name", result.client().getName());
        resp.put("clientType", result.client().getClientType());
        if (result.rawSecret() != null) {
            resp.put("clientSecret", result.rawSecret());
            resp.put("warning", "Save this secret – it will not be shown again.");
        }
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam String userId) {
        return ResponseEntity.ok(clientService.getUserClients("USER", userId));
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<?> get(@PathVariable String clientId) {
        return ResponseEntity.ok(clientService.getClient(clientId));
    }

    @PostMapping("/{clientId}/secrets")
    public ResponseEntity<Map<String, Object>> rotateSecret(@PathVariable String clientId) {
        String newSecret = clientService.rotateSecret(clientId);
        return ResponseEntity.ok(Map.of("clientSecret", newSecret, "warning", "Save this secret – it will not be shown again."));
    }

    @PostMapping("/{clientId}/suspend")
    public ResponseEntity<?> suspend(@PathVariable String clientId) {
        clientService.suspendClient(clientId);
        return ResponseEntity.ok(Map.of("status", "SUSPENDED"));
    }
}