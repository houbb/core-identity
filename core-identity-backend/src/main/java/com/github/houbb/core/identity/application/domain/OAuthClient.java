package com.github.houbb.core.identity.application.domain;

/**
 * OAuth Client — a registered application that can request OAuth tokens.
 *
 * Table: identity_oauth_client
 */
public class OAuthClient {
    private String id;
    private String clientId;
    private String ownerType;
    private String ownerId;
    private String clientType;
    private String name;
    private String description;
    private String homepageUrl;
    private String logoObjectId;
    private String privacyPolicyUrl;
    private String termsUrl;
    private String status;
    private String reviewStatus;
    private int consentRequired;
    private String createdBy;
    private long createdAt;
    private long updatedAt;
    private long version;

    public OAuthClient() {}
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getOwnerType() { return ownerType; }
    public void setOwnerType(String ownerType) { this.ownerType = ownerType; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getClientType() { return clientType; }
    public void setClientType(String clientType) { this.clientType = clientType; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getHomepageUrl() { return homepageUrl; }
    public void setHomepageUrl(String homepageUrl) { this.homepageUrl = homepageUrl; }
    public String getLogoObjectId() { return logoObjectId; }
    public void setLogoObjectId(String logoObjectId) { this.logoObjectId = logoObjectId; }
    public String getPrivacyPolicyUrl() { return privacyPolicyUrl; }
    public void setPrivacyPolicyUrl(String privacyPolicyUrl) { this.privacyPolicyUrl = privacyPolicyUrl; }
    public String getTermsUrl() { return termsUrl; }
    public void setTermsUrl(String termsUrl) { this.termsUrl = termsUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }
    public int getConsentRequired() { return consentRequired; }
    public void setConsentRequired(int consentRequired) { this.consentRequired = consentRequired; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}