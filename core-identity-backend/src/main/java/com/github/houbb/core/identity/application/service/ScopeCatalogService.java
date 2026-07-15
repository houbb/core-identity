package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.Scope;
import com.github.houbb.core.identity.application.domain.ScopePermission;

import java.util.List;

/**
 * Scope and Audience catalog management.
 */
public interface ScopeCatalogService {

    /**
     * Idempotent sync of scopes from an external service manifest.
     */
    List<Scope> syncScopes(String serviceName, String manifestVersion,
                           List<ScopeManifestEntry> entries, String syncedBy);

    /**
     * Sync scope-to-permission mappings.
     */
    void syncScopePermissions(String scopeId, List<String> permissionIds, String syncedBy);

    /**
     * Get all assignable scopes with optional filters.
     */
    List<Scope> getAssignableScopes(String service, String audienceCode, String riskLevel);

    /**
     * Get scopes by their IDs.
     */
    List<Scope> getScopesByIds(List<String> scopeIds);

    /**
     * Get all scopes.
     */
    List<Scope> getAllScopes();

    /**
     * Get all assigned permissions for a scope.
     */
    List<ScopePermission> getScopePermissions(String scopeId);

    /**
     * Get all scope-permission mappings for given scope IDs.
     */
    List<ScopePermission> getScopePermissionsForScopes(List<String> scopeIds);

    record ScopeManifestEntry(
            String code,
            String name,
            String audienceCode,
            String riskLevel,
            String consentDisplay,
            String description
    ) {
    }
}