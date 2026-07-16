package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.ScimGroupRoleMapping;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_scim_group_role_mapping.
 */
public interface ScimGroupRoleMappingRepository {
    void save(ScimGroupRoleMapping mapping);
    Optional<ScimGroupRoleMapping> findById(String id);
    List<ScimGroupRoleMapping> findByGroupId(String groupId);
    List<ScimGroupRoleMapping> findByRoleId(String roleId);
    void update(ScimGroupRoleMapping mapping);
    void deleteById(String id);
}
