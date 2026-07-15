package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.Membership;

import java.util.List;
import java.util.Map;

/**
 * Membership management service — member list, roles, suspend/remove/leave.
 */
public interface MembershipService {

    List<MembershipDTO> getMembers(String organizationId, String status, String role, String search);

    MembershipDTO getMember(String organizationId, String membershipId);

    void updateMemberRoles(String organizationId, String membershipId, List<String> roleIds, String operatorId);

    void suspendMember(String organizationId, String membershipId, String operatorId);

    void reactivateMember(String organizationId, String membershipId, String operatorId);

    void removeMember(String organizationId, String membershipId, String operatorId);

    void leaveOrganization(String organizationId, String userId);

    record MembershipDTO(
            String membershipId,
            String organizationId,
            String userId,
            String displayName,
            String email,
            String status,
            String source,
            List<String> roleNames,
            List<String> roleIds,
            long joinedAt,
            Long lastAccessedAt,
            long createdAt
    ) {}
}