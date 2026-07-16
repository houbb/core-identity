package com.github.houbb.core.identity.application.domain;

/**
 * Membership domain object — user-to-organization relationship.
 *
 * P2 extended fields: source, lifecycle timestamps, lastAccessedAt, createdBy.
 *
 * P5 extended fields: managementSource, managedByConnectionId, externalResourceId,
 * provisionedAt, deprovisionedAt.
 */
public class Membership {

    private String id;
    private String organizationId;
    private String userId;
    private String membershipType;
    private String status;
    private String source;
    private long joinedAt;
    private Long leftAt;
    private Long removedAt;
    private Long suspendedAt;
    private Long lastAccessedAt;
    private String createdBy;
    private String managementSource;
    private String managedByConnectionId;
    private String externalResourceId;
    private Long provisionedAt;
    private Long deprovisionedAt;
    private long createdAt;
    private long updatedAt;
    private long version;

    public Membership() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getMembershipType() { return membershipType; }
    public void setMembershipType(String membershipType) { this.membershipType = membershipType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public long getJoinedAt() { return joinedAt; }
    public void setJoinedAt(long joinedAt) { this.joinedAt = joinedAt; }
    public Long getLeftAt() { return leftAt; }
    public void setLeftAt(Long leftAt) { this.leftAt = leftAt; }
    public Long getRemovedAt() { return removedAt; }
    public void setRemovedAt(Long removedAt) { this.removedAt = removedAt; }
    public Long getSuspendedAt() { return suspendedAt; }
    public void setSuspendedAt(Long suspendedAt) { this.suspendedAt = suspendedAt; }
    public Long getLastAccessedAt() { return lastAccessedAt; }
    public void setLastAccessedAt(Long lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getManagementSource() { return managementSource; }
    public void setManagementSource(String managementSource) { this.managementSource = managementSource; }
    public String getManagedByConnectionId() { return managedByConnectionId; }
    public void setManagedByConnectionId(String managedByConnectionId) { this.managedByConnectionId = managedByConnectionId; }
    public String getExternalResourceId() { return externalResourceId; }
    public void setExternalResourceId(String externalResourceId) { this.externalResourceId = externalResourceId; }
    public Long getProvisionedAt() { return provisionedAt; }
    public void setProvisionedAt(Long provisionedAt) { this.provisionedAt = provisionedAt; }
    public Long getDeprovisionedAt() { return deprovisionedAt; }
    public void setDeprovisionedAt(Long deprovisionedAt) { this.deprovisionedAt = deprovisionedAt; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}