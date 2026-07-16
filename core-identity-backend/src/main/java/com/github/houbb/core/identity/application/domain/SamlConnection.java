package com.github.houbb.core.identity.application.domain;

/**
 * SAML Connection domain object — SAML 2.0 specific federation configuration.
 *
 * P5: Stores SAML SP/IdP metadata, endpoint URLs, signing policies, and attribute mapping.
 * Table: identity_saml_connection
 */
public class SamlConnection {

    private String connectionId;
    private String spEntityId;
    private String acsUrl;
    private String idpEntityId;
    private String idpSsoUrl;
    private String idpSloUrl;
    private String nameIdFormat;
    private String subjectAttribute;
    private String emailAttribute;
    private String nameAttribute;
    private String groupsAttribute;
    private int signAuthnRequests;
    private int requireSignedResponse;
    private int requireSignedAssertion;
    private int requireEncryptedAssertion;
    private int clockSkewSeconds;
    private String metadataXmlEncrypted;
    private Long metadataFetchedAt;
    private long createdAt;
    private long updatedAt;
    private long version;

    public SamlConnection() {
    }

    public String getConnectionId() { return connectionId; }
    public void setConnectionId(String connectionId) { this.connectionId = connectionId; }
    public String getSpEntityId() { return spEntityId; }
    public void setSpEntityId(String spEntityId) { this.spEntityId = spEntityId; }
    public String getAcsUrl() { return acsUrl; }
    public void setAcsUrl(String acsUrl) { this.acsUrl = acsUrl; }
    public String getIdpEntityId() { return idpEntityId; }
    public void setIdpEntityId(String idpEntityId) { this.idpEntityId = idpEntityId; }
    public String getIdpSsoUrl() { return idpSsoUrl; }
    public void setIdpSsoUrl(String idpSsoUrl) { this.idpSsoUrl = idpSsoUrl; }
    public String getIdpSloUrl() { return idpSloUrl; }
    public void setIdpSloUrl(String idpSloUrl) { this.idpSloUrl = idpSloUrl; }
    public String getNameIdFormat() { return nameIdFormat; }
    public void setNameIdFormat(String nameIdFormat) { this.nameIdFormat = nameIdFormat; }
    public String getSubjectAttribute() { return subjectAttribute; }
    public void setSubjectAttribute(String subjectAttribute) { this.subjectAttribute = subjectAttribute; }
    public String getEmailAttribute() { return emailAttribute; }
    public void setEmailAttribute(String emailAttribute) { this.emailAttribute = emailAttribute; }
    public String getNameAttribute() { return nameAttribute; }
    public void setNameAttribute(String nameAttribute) { this.nameAttribute = nameAttribute; }
    public String getGroupsAttribute() { return groupsAttribute; }
    public void setGroupsAttribute(String groupsAttribute) { this.groupsAttribute = groupsAttribute; }
    public int getSignAuthnRequests() { return signAuthnRequests; }
    public void setSignAuthnRequests(int signAuthnRequests) { this.signAuthnRequests = signAuthnRequests; }
    public int getRequireSignedResponse() { return requireSignedResponse; }
    public void setRequireSignedResponse(int requireSignedResponse) { this.requireSignedResponse = requireSignedResponse; }
    public int getRequireSignedAssertion() { return requireSignedAssertion; }
    public void setRequireSignedAssertion(int requireSignedAssertion) { this.requireSignedAssertion = requireSignedAssertion; }
    public int getRequireEncryptedAssertion() { return requireEncryptedAssertion; }
    public void setRequireEncryptedAssertion(int requireEncryptedAssertion) { this.requireEncryptedAssertion = requireEncryptedAssertion; }
    public int getClockSkewSeconds() { return clockSkewSeconds; }
    public void setClockSkewSeconds(int clockSkewSeconds) { this.clockSkewSeconds = clockSkewSeconds; }
    public String getMetadataXmlEncrypted() { return metadataXmlEncrypted; }
    public void setMetadataXmlEncrypted(String metadataXmlEncrypted) { this.metadataXmlEncrypted = metadataXmlEncrypted; }
    public Long getMetadataFetchedAt() { return metadataFetchedAt; }
    public void setMetadataFetchedAt(Long metadataFetchedAt) { this.metadataFetchedAt = metadataFetchedAt; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
