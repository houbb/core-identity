package com.github.houbb.core.identity.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Degradation manager (P7.6).
 * <p>
 * Tracks the health of external dependencies and manages
 * graceful degradation when they fail. Ensures core authentication
 * and authorization work even when dependencies are unavailable.
 */
public class DegradationManager {

    private static final Logger log = LoggerFactory.getLogger(DegradationManager.class);

    private final Map<String, ComponentStatus> components = new ConcurrentHashMap<>();

    public DegradationManager() {
        components.put("redis", new ComponentStatus("redis", true));
        components.put("notification", new ComponentStatus("notification", true));
        components.put("billing", new ComponentStatus("billing", true));
        components.put("storage", new ComponentStatus("storage", true));
        components.put("event-broker", new ComponentStatus("event-broker", true));
    }

    /**
     * Mark a component as degraded.
     */
    public void markDegraded(String component, String reason) {
        ComponentStatus status = components.get(component);
        if (status != null) {
            status.healthy.set(false);
            status.lastFailureReason = reason;
            status.lastFailureTime = System.currentTimeMillis();
            log.warn("Component DEGRADED: {} — {}", component, reason);
        }
    }

    /**
     * Mark a component as healthy.
     */
    public void markHealthy(String component) {
        ComponentStatus status = components.get(component);
        if (status != null) {
            status.healthy.set(true);
            status.lastRecoveryTime = System.currentTimeMillis();
            log.info("Component RECOVERED: {}", component);
        }
    }

    /**
     * Check if a component is healthy.
     */
    public boolean isHealthy(String component) {
        ComponentStatus status = components.get(component);
        return status == null || status.healthy.get();
    }

    /**
     * Get the overall system status.
     */
    public Map<String, Object> getSystemStatus() {
        Map<String, Object> result = new LinkedHashMap<>();
        boolean allHealthy = true;

        for (Map.Entry<String, ComponentStatus> entry : components.entrySet()) {
            ComponentStatus cs = entry.getValue();
            Map<String, Object> comp = new LinkedHashMap<>();
            comp.put("healthy", cs.healthy.get());
            if (!cs.healthy.get()) {
                comp.put("failureReason", cs.lastFailureReason);
                comp.put("failureTime", cs.lastFailureTime);
                allHealthy = false;
            }
            result.put(entry.getKey(), comp);
        }

        result.put("overall", allHealthy ? "HEALTHY" : "DEGRADED");
        return result;
    }

    /**
     * Get the Redis component status.
     * Used by SessionStore and CacheManager to check before using Redis.
     */
    public boolean isRedisAvailable() {
        return isHealthy("redis");
    }

    private static class ComponentStatus {
        final String name;
        final AtomicBoolean healthy;
        volatile String lastFailureReason;
        volatile long lastFailureTime;
        volatile long lastRecoveryTime;

        ComponentStatus(String name, boolean healthy) {
            this.name = name;
            this.healthy = new AtomicBoolean(healthy);
        }
    }
}
