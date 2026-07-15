package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.ServiceAccountRole;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_service_account_role.
 */
public interface ServiceAccountRoleRepository {

    void save(ServiceAccountRole role);

    List<ServiceAccountRole> findByServiceAccountId(String serviceAccountId);

    Optional<ServiceAccountRole> findByServiceAccountAndRole(String serviceAccountId, String roleId);

    void deleteByServiceAccountId(String serviceAccountId);

    void deleteByServiceAccountIdAndRoleId(String serviceAccountId, String roleId);
}