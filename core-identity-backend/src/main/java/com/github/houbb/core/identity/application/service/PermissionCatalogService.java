package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.Permission;

import java.util.List;

/**
 * Permission catalog management — sync and query permissions.
 */
public interface PermissionCatalogService {

    /**
     * Idempotent sync of permissions from an external service manifest.
     * New permissions → ACTIVE; existing → update name/description; removed (not in manifest) → DEPRECATED.
     */
    List<Permission> syncPermissions(String serviceName, String manifestVersion,
                                     List<PermissionManifestEntry> entries, String syncedBy);

    /**
     * Get all currently assignable (ACTIVE + assignable=1) permissions with optional filters.
     */
    List<Permission> getAssignablePermissions(String service, String resource, String riskLevel, String search);

    /**
     * Get all permissions across all services.
     */
    List<Permission> getAllPermissions();

    /**
     * Record for a single permission entry in a service manifest.
     */
    record PermissionManifestEntry(
            String code,
            String name,
            String resource,
            String action,
            String riskLevel,
            String description
    ) {
    }
}