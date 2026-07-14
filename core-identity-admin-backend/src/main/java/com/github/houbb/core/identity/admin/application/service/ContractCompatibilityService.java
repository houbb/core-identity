package com.github.houbb.core.identity.admin.application.service;

/**
 * Contract compatibility service.
 */
public interface ContractCompatibilityService {

    /**
     * Check if contracts are compatible between web and backend.
     */
    String checkCompatibility();

    /**
     * Get contract version info.
     */
    java.util.Map<String, Object> getContractVersions();
}