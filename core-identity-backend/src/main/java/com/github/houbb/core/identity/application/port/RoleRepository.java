package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.Role;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_role.
 */
public interface RoleRepository {

    void save(Role role);

    Optional<Role> findById(String id);

    Optional<Role> findByOrgAndKey(String organizationId, String roleKey);

    List<Role> findByOrgId(String organizationId);

    List<Role> findByIds(List<String> ids);

    boolean existsByNameInOrg(String organizationId, String name, String excludeId);

    void update(Role role);

    void deleteById(String id, long version);

    int countMembersByRoleId(String roleId);
}