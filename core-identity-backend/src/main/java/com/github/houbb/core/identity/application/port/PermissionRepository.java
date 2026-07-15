package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.Permission;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_permission.
 */
public interface PermissionRepository {

    void save(Permission permission);

    Optional<Permission> findById(String id);

    Optional<Permission> findByCode(String permissionCode);

    List<Permission> findByService(String sourceService);

    List<Permission> findByServices(List<String> services);

    List<Permission> findAllAssignable();

    List<Permission> findAll();

    List<Permission> findAssignableByService(String service, String resource, String riskLevel, String search);

    void update(Permission permission);

    void updateStatus(String id, String status, long now, long version);

    int countByService(String sourceService);
}