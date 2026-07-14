package com.github.houbb.core.identity.admin.application.service;

import java.util.Map;

/**
 * Service overview aggregation service.
 */
public interface SystemOverviewService {

    /**
     * Aggregate system status from admin backend + identity backend + database.
     */
    Map<String, Object> getOverview();
}