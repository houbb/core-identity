package com.github.houbb.core.identity.application.domain;

/**
 * MembershipRole — many-to-many link between Membership and Role.
 *
 * Table: identity_membership_role
 */
public class MembershipRole {

    private String membershipId;
    private String roleId;
    private String assignedBy;
    private long createdAt;

    public MembershipRole() {
    }

    public String getMembershipId() { return membershipId; }
    public void setMembershipId(String membershipId) { this.membershipId = membershipId; }
    public String getRoleId() { return roleId; }
    public void setRoleId(String roleId) { this.roleId = roleId; }
    public String getAssignedBy() { return assignedBy; }
    public void setAssignedBy(String assignedBy) { this.assignedBy = assignedBy; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}