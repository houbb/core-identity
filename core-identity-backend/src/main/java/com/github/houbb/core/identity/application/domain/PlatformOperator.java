package com.github.houbb.core.identity.application.domain;

/**
 * Platform operator domain object — marks users who can access the admin console.
 */
public class PlatformOperator {

    private String id;
    private String userId;
    private String operatorRole;
    private String status;
    private String grantedBy;
    private long grantedAt;
    private Long disabledAt;
    private long createdAt;
    private long updatedAt;
    private long version;

    public PlatformOperator() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getOperatorRole() { return operatorRole; }
    public void setOperatorRole(String operatorRole) { this.operatorRole = operatorRole; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getGrantedBy() { return grantedBy; }
    public void setGrantedBy(String grantedBy) { this.grantedBy = grantedBy; }
    public long getGrantedAt() { return grantedAt; }
    public void setGrantedAt(long grantedAt) { this.grantedAt = grantedAt; }
    public Long getDisabledAt() { return disabledAt; }
    public void setDisabledAt(Long disabledAt) { this.disabledAt = disabledAt; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}