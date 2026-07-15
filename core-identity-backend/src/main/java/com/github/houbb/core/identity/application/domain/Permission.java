package com.github.houbb.core.identity.application.domain;

/**
 * Permission — a single assignable permission code in the unified catalog.
 *
 * Table: identity_permission
 */
public class Permission {

    private String id;
    private String permissionCode;
    private String sourceService;
    private String resource;
    private String action;
    private String name;
    private String description;
    private String riskLevel;
    private int assignable;
    private String status;
    private String sourceVersion;
    private long createdAt;
    private long updatedAt;
    private long version;

    public Permission() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPermissionCode() { return permissionCode; }
    public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }
    public String getSourceService() { return sourceService; }
    public void setSourceService(String sourceService) { this.sourceService = sourceService; }
    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public int getAssignable() { return assignable; }
    public void setAssignable(int assignable) { this.assignable = assignable; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSourceVersion() { return sourceVersion; }
    public void setSourceVersion(String sourceVersion) { this.sourceVersion = sourceVersion; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}