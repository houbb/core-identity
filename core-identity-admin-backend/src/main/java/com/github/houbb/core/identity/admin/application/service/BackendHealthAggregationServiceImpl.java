package com.github.houbb.core.identity.admin.application.service;

import com.github.houbb.core.identity.admin.infrastructure.identityclient.IdentityInternalClient;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Default implementation of BackendHealthAggregationService.
 */
@Service
public class BackendHealthAggregationServiceImpl implements BackendHealthAggregationService {

    private final IdentityInternalClient identityClient;

    public BackendHealthAggregationServiceImpl(IdentityInternalClient identityClient) {
        this.identityClient = identityClient;
    }

    @Override
    public String getAggregatedHealth() {
        if (identityClient.isReachable()) {
            return "HEALTHY";
        }
        return "DEGRADED";
    }

    @Override
    public Map<String, Object> getDetailedHealth() {
        Map<String, Object> health = new LinkedHashMap<>();

        // Admin Backend self-health
        Map<String, Object> admin = new LinkedHashMap<>();
        admin.put("status", "HEALTHY");
        admin.put("service", "core-identity-admin-backend");
        health.put("adminBackend", admin);

        // Identity Backend health
        Map<String, Object> identityHealth = identityClient.getHealthInfo();
        health.put("identityBackend", identityHealth);

        health.put("aggregatedStatus", getAggregatedHealth());
        health.put("timestamp", System.currentTimeMillis());
        return health;
    }
}