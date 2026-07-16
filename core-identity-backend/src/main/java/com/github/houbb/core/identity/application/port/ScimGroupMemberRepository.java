package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.ScimGroupMember;

import java.util.List;

/**
 * Repository for identity_scim_group_member.
 */
public interface ScimGroupMemberRepository {
    void save(ScimGroupMember member);
    List<ScimGroupMember> findByGroupId(String groupId);
    List<ScimGroupMember> findByExternalIdentityId(String externalIdentityId);
    void deleteByGroupId(String groupId);
    void deleteByGroupIdAndExternalIdentityId(String groupId, String externalIdentityId);
}
