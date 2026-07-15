package com.github.houbb.core.identity.application.domain;

/**
 * Organization domain object — resource ownership container.
 */
public class Organization {

    private String id;
    private String organizationType;
    private String name;
    private String slug;
    private String personalOwnerUserId;
    private String status;
    private long createdAt;
    private long updatedAt;
    private long version;

    public Organization() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrganizationType() { return organizationType; }
    public void setOrganizationType(String organizationType) { this.organizationType = organizationType; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getPersonalOwnerUserId() { return personalOwnerUserId; }
    public void setPersonalOwnerUserId(String personalOwnerUserId) { this.personalOwnerUserId = personalOwnerUserId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}