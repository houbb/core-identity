package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.MembershipRole;

import java.util.List;

/**
 * Repository for identity_membership_role.
 */
public interface MembershipRoleRepository {

    void save(MembershipRole mr);

    void deleteByMembershipAndRole(String membershipId, String roleId);

    void deleteAllByMembershipId(String membershipId);

    List<MembershipRole> findByMembershipId(String membershipId);

    List<MembershipRole> findByRoleId(String roleId);

    List<String> findRoleIdsByMembershipId(String membershipId);

    List<String> findRoleIdsByMembershipIds(List<String> membershipIds);

    int countByRoleId(String roleId);

    void replaceRoles(String membershipId, List<String> roleIds, String assignedBy, long now);
}