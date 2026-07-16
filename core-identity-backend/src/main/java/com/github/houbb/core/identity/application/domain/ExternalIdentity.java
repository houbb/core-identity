package com.github.houbb.core.identity.application.domain;

/**
 * External Identity domain object — maps an external IdP subject to a local User.
 *
 * P5: The bridge between enterprise IdP authentication and Core Identity users.
 * Unique key: connectionId + externalSubject (never email).
 * Table: identity_external_identity
 */
public class ExternalIdentity {

    private String id;
    private String userId;
    private String organizationId;
    private String connectionId;
    private String externalSubject;
    private String externalUsername;
    private String externalEmail;
    private String externalEmployeeId;
    private String status;
    private String managementSource;
    private String claimsSnapshotJson;
    private Long firstLoginAt;
    private Long lastLoginAt;
    private Long linkedAt;
    private Long unlinkedAt;
    private long createdAt;
    private long updatedAt;
    private long version;

    public ExternalIdentity() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    public String getConnectionId() { return connectionId; }
    public void setConnectionId(String connectionId) { this.connectionId = connectionId; }
    public String getExternalSubject() { return externalSubject; }
    public void setExternalSubject(String externalSubject) { this.externalSubject = externalSubject; }
    public String getExternalUsername() { return externalUsername; }
    public void setExternalUsername(String externalUsername) { this.externalUsername = externalUsername; }
    public String getExternalEmail() { return externalEmail; }
    public void setExternalEmail(String externalEmail) { this.externalEmail = externalEmail; }
    public String getExternalEmployeeId() { return externalEmployeeId; }
    public void setExternalEmployeeId(String externalEmployeeId) { this.externalEmployeeId = externalEmployeeId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getManagementSource() { return managementSource; }
    public void setManagementSource(String managementSource) { this.managementSource = managementSource; }
    public String getClaimsSnapshotJson() { return claimsSnapshotJson; }
    public void setClaimsSnapshotJson(String claimsSnapshotJson) { this.claimsSnapshotJson = claimsSnapshotJson; }
    public Long getFirstLoginAt() { return firstLoginAt; }
    public void setFirstLoginAt(Long firstLoginAt) { this.firstLoginAt = firstLoginAt; }
    public Long getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(Long lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public Long getLinkedAt() { return linkedAt; }
    public void setLinkedAt(Long linkedAt) { this.linkedAt = linkedAt; }
    public Long getUnlinkedAt() { return unlinkedAt; }
    public void setUnlinkedAt(Long unlinkedAt) { this.unlinkedAt = unlinkedAt; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
