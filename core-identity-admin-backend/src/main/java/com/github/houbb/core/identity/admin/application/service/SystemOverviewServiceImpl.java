package com.github.houbb.core.identity.admin.application.service;

import com.github.houbb.core.identity.admin.infrastructure.identityclient.IdentityInternalClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

/**
 * Default implementation of SystemOverviewService.
 * Aggregates system status from admin backend + identity backend.
 */
@Service
public class SystemOverviewServiceImpl implements SystemOverviewService {

    private final IdentityInternalClient identityClient;
    private final long bootTime;

    public SystemOverviewServiceImpl(IdentityInternalClient identityClient) {
        this.identityClient = identityClient;
        this.bootTime = System.currentTimeMillis();
    }

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("timestamp", Instant.now().toString());

        // Admin Backend status
        Map<String, Object> adminSelf = new LinkedHashMap<>();
        adminSelf.put("service", "core-identity-admin-backend");
        adminSelf.put("version", "0.1.0");
        adminSelf.put("status", "HEALTHY");
        adminSelf.put("bootTime", Instant.ofEpochMilli(bootTime).toString());
        adminSelf.put("uptime", System.currentTimeMillis() - bootTime);
        overview.put("adminBackend", adminSelf);

        // Identity Backend status
        Map<String, Object> identityBackend = new LinkedHashMap<>();
        identityBackend.put("service", "core-identity-backend");
        identityBackend.put("baseUrl", "http://localhost:8101");
        identityBackend.put("reachable", identityClient.isReachable());

        Map<String, Object> identityInfo = identityClient.getSystemInfo();
        identityBackend.put("info", identityInfo);
        identityBackend.put("status", identityInfo.containsKey("error") ? "DEGRADED" : "HEALTHY");

        Map<String, Object> identityHealth = identityClient.getHealthInfo();
        identityBackend.put("health", identityHealth);
        overview.put("identityBackend", identityBackend);

        // Overall status
        String overallStatus = identityClient.isReachable() ? "HEALTHY" : "DEGRADED";
        overview.put("overallStatus", overallStatus);

        return overview;
    }
}