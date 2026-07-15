package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.ScopePermission;

import java.util.List;

/**
 * Repository for identity_scope_permission.
 */
public interface ScopePermissionRepository {

    void save(ScopePermission mapping);

    void saveAll(List<ScopePermission> mappings);

    List<ScopePermission> findByScopeId(String scopeId);

    List<ScopePermission> findByPermissionId(String permissionId);

    List<ScopePermission> findByScopeIds(List<String> scopeIds);

    void deleteByScopeId(String scopeId);

    void deleteByScopeIdAndPermissionId(String scopeId, String permissionId);
}