package com.github.houbb.core.identity.application.domain;

/**
 * Organization domain object — resource ownership container.
 *
 * P2 extended fields: ownerUserId, description, lifecycle timestamps,
 * logoObjectId, authorizationVersion.
 */
public class Organization {

    private String id;
    private String organizationType;
    private String name;
    private String slug;
    private String personalOwnerUserId;
    private String ownerUserId;
    private String description;
    private String status;
    private String logoObjectId;
    private Long suspendedAt;
    private String suspendedReason;
    private Long deletionRequestedAt;
    private Long deletionEffectiveAt;
    private long authorizationVersion;
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
    public String getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(String ownerUserId) { this.ownerUserId = ownerUserId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLogoObjectId() { return logoObjectId; }
    public void setLogoObjectId(String logoObjectId) { this.logoObjectId = logoObjectId; }
    public Long getSuspendedAt() { return suspendedAt; }
    public void setSuspendedAt(Long suspendedAt) { this.suspendedAt = suspendedAt; }
    public String getSuspendedReason() { return suspendedReason; }
    public void setSuspendedReason(String suspendedReason) { this.suspendedReason = suspendedReason; }
    public Long getDeletionRequestedAt() { return deletionRequestedAt; }
    public void setDeletionRequestedAt(Long deletionRequestedAt) { this.deletionRequestedAt = deletionRequestedAt; }
    public Long getDeletionEffectiveAt() { return deletionEffectiveAt; }
    public void setDeletionEffectiveAt(Long deletionEffectiveAt) { this.deletionEffectiveAt = deletionEffectiveAt; }
    public long getAuthorizationVersion() { return authorizationVersion; }
    public void setAuthorizationVersion(long authorizationVersion) { this.authorizationVersion = authorizationVersion; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}