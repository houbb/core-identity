package com.github.houbb.core.identity.admin.application.service;

/**
 * Health aggregation service.
 */
public interface BackendHealthAggregationService {

    /**
     * Aggregate health from all backend services.
     */
    String getAggregatedHealth();

    /**
     * Get detailed health info.
     */
    java.util.Map<String, Object> getDetailedHealth();
}