package com.github.houbb.core.identity.application.port;

import java.util.List;

/**
 * Repository for identity_platform_operator_role.
 */
public interface PlatformOperatorRoleRepository {

    void save(String id, String operatorId, String roleCode, String grantedBy, long grantedAt, long createdAt);

    void delete(String operatorId, String roleCode);

    List<String> findRoleCodesByOperatorId(String operatorId);

    int countByOperatorAndRole(String operatorId, String roleCode);
}
