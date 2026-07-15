package com.github.houbb.core.identity.application.domain;

public class ServiceAccount {
    private String id; private String organizationId; private String accountType; private String name;
    private String description; private String status; private Long lastUsedAt; private String createdBy;
    private long createdAt; private long updatedAt; private long version;
    public ServiceAccount() {}
    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getOrganizationId() { return organizationId; } public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    public String getAccountType() { return accountType; } public void setAccountType(String accountType) { this.accountType = accountType; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public Long getLastUsedAt() { return lastUsedAt; } public void setLastUsedAt(Long lastUsedAt) { this.lastUsedAt = lastUsedAt; }
    public String getCreatedBy() { return createdBy; } public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public long getCreatedAt() { return createdAt; } public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; } public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; } public void setVersion(long version) { this.version = version; }
}