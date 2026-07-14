package com.github.houbb.core.identity.admin.infrastructure.identityclient;

import java.util.Map;

/**
 * HTTP client for calling Identity Backend Internal API.
 * Generated from OpenAPI contract, thin wrapper over HTTP.
 */
public interface IdentityInternalClient {

    /**
     * Get system info from Identity Backend.
     */
    Map<String, Object> getSystemInfo();

    /**
     * Get health info from Identity Backend.
     */
    Map<String, Object> getHealthInfo();

    /**
     * Check if Identity Backend is reachable.
     */
    boolean isReachable();
}