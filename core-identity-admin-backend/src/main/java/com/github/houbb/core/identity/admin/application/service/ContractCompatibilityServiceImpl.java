package com.github.houbb.core.identity.admin.application.service;

import com.github.houbb.core.identity.admin.infrastructure.identityclient.IdentityInternalClient;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Default implementation of ContractCompatibilityService.
 */
@Service
public class ContractCompatibilityServiceImpl implements ContractCompatibilityService {

    private final IdentityInternalClient identityClient;

    public ContractCompatibilityServiceImpl(IdentityInternalClient identityClient) {
        this.identityClient = identityClient;
    }

    @Override
    public String checkCompatibility() {
        Map<String, Object> info = identityClient.getSystemInfo();
        if (info.containsKey("error")) {
            return "UNAVAILABLE";
        }
        String apiVersion = (String) info.getOrDefault("apiVersion", "v1");
        if ("v1".equals(apiVersion)) {
            return "COMPATIBLE";
        }
        return "INCOMPATIBLE";
    }

    @Override
    public Map<String, Object> getContractVersions() {
        Map<String, Object> versions = new LinkedHashMap<>();
        versions.put("adminBackendVersion", "0.1.0");
        versions.put("adminApiVersion", "v1");

        Map<String, Object> info = identityClient.getSystemInfo();
        if (!info.containsKey("error")) {
            versions.put("identityBackendVersion", info.getOrDefault("version", "unknown"));
            versions.put("identityApiVersion", info.getOrDefault("apiVersion", "unknown"));
        }

        versions.put("compatibility", checkCompatibility());
        return versions;
    }
}