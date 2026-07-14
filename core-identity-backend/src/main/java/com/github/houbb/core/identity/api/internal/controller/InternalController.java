package com.github.houbb.core.identity.api.internal.controller;

import com.github.houbb.core.identity.api.response.HealthResponse;
import com.github.houbb.core.identity.api.response.ServiceTokenResponse;
import com.github.houbb.core.identity.api.response.SystemInfoResponse;
import com.github.houbb.core.identity.application.service.AuditService;
import com.github.houbb.core.identity.application.service.InternalTokenService;
import com.github.houbb.core.identity.application.service.SystemInfoService;
import com.github.houbb.core.identity.application.command.AuditCommand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Internal API: service tokens, system info, health, audit events.
 * Must be protected by InternalAuthFilter in production.
 */
@RestController
@RequestMapping("/internal/v1/identity")
public class InternalController {

    private final InternalTokenService tokenService;
    private final SystemInfoService systemInfoService;
    private final AuditService auditService;

    public InternalController(InternalTokenService tokenService,
                              SystemInfoService systemInfoService,
                              AuditService auditService) {
        this.tokenService = tokenService;
        this.systemInfoService = systemInfoService;
        this.auditService = auditService;
    }

    @PostMapping("/service-tokens")
    public ServiceTokenResponse createServiceToken(@RequestBody Map<String, String> body) {
        String clientId = body.get("client_id");
        String clientSecret = body.get("client_secret");

        if (clientId == null || clientSecret == null) {
            throw new IllegalArgumentException("client_id and client_secret are required");
        }

        String token = tokenService.issueToken(clientId, clientSecret);
        return new ServiceTokenResponse(token, "Bearer", 600, "identity.system.read identity.audit.write");
    }

    @GetMapping("/system/info")
    public SystemInfoResponse getSystemInfo() {
        SystemInfoResponse response = new SystemInfoResponse();
        response.setService("core-identity-backend");
        response.setVersion(systemInfoService.getVersion());
        response.setApiVersion(systemInfoService.getApiVersion());
        response.setDatabaseType("SQLite");
        response.setDatabaseStatus("CONNECTED");
        response.setFlywayStatus("MIGRATED");
        response.setBootTime(System.currentTimeMillis());
        return response;
    }

    @GetMapping("/system/health")
    public HealthResponse getHealth() {
        return new HealthResponse("HEALTHY", "core-identity-backend", System.currentTimeMillis());
    }

    @PostMapping("/audit-events")
    public ResponseEntity<Map<String, String>> recordAudit(@RequestBody AuditCommand command) {
        String eventId = auditService.record(command);
        return ResponseEntity.ok(Map.of("eventId", eventId));
    }
}