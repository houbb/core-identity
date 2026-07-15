package com.github.houbb.core.identity.application.domain;

/**
 * Invitation — an invitation to join an organization.
 *
 * Table: identity_invitation
 */
public class Invitation {

    private String id;
    private String organizationId;
    private String emailNormalized;
    private String emailDisplay;
    private String tokenHash;
    private String status;
    private String invitedByUserId;
    private String acceptedByUserId;
    private String message;
    private long expiresAt;
    private Long acceptedAt;
    private Long declinedAt;
    private Long revokedAt;
    private long createdAt;
    private long updatedAt;
    private long version;

    public Invitation() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    public String getEmailNormalized() { return emailNormalized; }
    public void setEmailNormalized(String emailNormalized) { this.emailNormalized = emailNormalized; }
    public String getEmailDisplay() { return emailDisplay; }
    public void setEmailDisplay(String emailDisplay) { this.emailDisplay = emailDisplay; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getInvitedByUserId() { return invitedByUserId; }
    public void setInvitedByUserId(String invitedByUserId) { this.invitedByUserId = invitedByUserId; }
    public String getAcceptedByUserId() { return acceptedByUserId; }
    public void setAcceptedByUserId(String acceptedByUserId) { this.acceptedByUserId = acceptedByUserId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }
    public Long getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(Long acceptedAt) { this.acceptedAt = acceptedAt; }
    public Long getDeclinedAt() { return declinedAt; }
    public void setDeclinedAt(Long declinedAt) { this.declinedAt = declinedAt; }
    public Long getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Long revokedAt) { this.revokedAt = revokedAt; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}