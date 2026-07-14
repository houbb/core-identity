package com.github.houbb.core.identity.application.service;

/**
 * System information service.
 */
public interface SystemInfoService {

    /**
     * Get a human-readable system info summary.
     */
    String getSystemInfo();

    /**
     * Get the current application version.
     */
    String getVersion();

    /**
     * Get the API version.
     */
    String getApiVersion();

    /**
     * Get the current service status.
     */
    String getStatus();

    /**
     * Get the list of available capabilities.
     */
    String[] getCapabilities();
}