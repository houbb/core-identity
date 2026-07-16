package com.github.houbb.core.identity.application.domain;

/**
 * SCIM Group Member domain object — many-to-many relationship between SCIM groups and external identities.
 *
 * P5: Join table linking ScimGroup with ExternalIdentity/Membership.
 * Primary key: groupId + externalIdentityId.
 * Table: identity_scim_group_member
 */
public class ScimGroupMember {

    private String groupId;
    private String externalIdentityId;
    private String membershipId;
    private long createdAt;

    public ScimGroupMember() {
    }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public String getExternalIdentityId() { return externalIdentityId; }
    public void setExternalIdentityId(String externalIdentityId) { this.externalIdentityId = externalIdentityId; }
    public String getMembershipId() { return membershipId; }
    public void setMembershipId(String membershipId) { this.membershipId = membershipId; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
