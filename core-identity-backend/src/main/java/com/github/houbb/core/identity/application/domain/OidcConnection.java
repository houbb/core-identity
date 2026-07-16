package com.github.houbb.core.identity.application.domain;

/**
 * OIDC Connection domain object — OpenID Connect specific federation configuration.
 *
 * P5: Stores OIDC provider endpoints, client credentials, and claim mapping rules.
 * Client secret must be encrypted at rest (AES-256-GCM).
 * Table: identity_oidc_connection
 */
public class OidcConnection {

    private String connectionId;
    private String issuer;
    private String discoveryUri;
    private String clientId;
    private String encryptedClientSecret;
    private String secretKeyVersion;
    private String scopesJson;
    private String subjectClaim;
    private String emailClaim;
    private String nameClaim;
    private String groupsClaim;
    private int requireEmailVerified;
    private int userinfoEnabled;
    private String logoutEndpoint;
    private String configurationCacheJson;
    private Long configurationFetchedAt;
    private long createdAt;
    private long updatedAt;
    private long version;

    public OidcConnection() {
    }

    public String getConnectionId() { return connectionId; }
    public void setConnectionId(String connectionId) { this.connectionId = connectionId; }
    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    public String getDiscoveryUri() { return discoveryUri; }
    public void setDiscoveryUri(String discoveryUri) { this.discoveryUri = discoveryUri; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getEncryptedClientSecret() { return encryptedClientSecret; }
    public void setEncryptedClientSecret(String encryptedClientSecret) { this.encryptedClientSecret = encryptedClientSecret; }
    public String getSecretKeyVersion() { return secretKeyVersion; }
    public void setSecretKeyVersion(String secretKeyVersion) { this.secretKeyVersion = secretKeyVersion; }
    public String getScopesJson() { return scopesJson; }
    public void setScopesJson(String scopesJson) { this.scopesJson = scopesJson; }
    public String getSubjectClaim() { return subjectClaim; }
    public void setSubjectClaim(String subjectClaim) { this.subjectClaim = subjectClaim; }
    public String getEmailClaim() { return emailClaim; }
    public void setEmailClaim(String emailClaim) { this.emailClaim = emailClaim; }
    public String getNameClaim() { return nameClaim; }
    public void setNameClaim(String nameClaim) { this.nameClaim = nameClaim; }
    public String getGroupsClaim() { return groupsClaim; }
    public void setGroupsClaim(String groupsClaim) { this.groupsClaim = groupsClaim; }
    public int getRequireEmailVerified() { return requireEmailVerified; }
    public void setRequireEmailVerified(int requireEmailVerified) { this.requireEmailVerified = requireEmailVerified; }
    public int getUserinfoEnabled() { return userinfoEnabled; }
    public void setUserinfoEnabled(int userinfoEnabled) { this.userinfoEnabled = userinfoEnabled; }
    public String getLogoutEndpoint() { return logoutEndpoint; }
    public void setLogoutEndpoint(String logoutEndpoint) { this.logoutEndpoint = logoutEndpoint; }
    public String getConfigurationCacheJson() { return configurationCacheJson; }
    public void setConfigurationCacheJson(String configurationCacheJson) { this.configurationCacheJson = configurationCacheJson; }
    public Long getConfigurationFetchedAt() { return configurationFetchedAt; }
    public void setConfigurationFetchedAt(Long configurationFetchedAt) { this.configurationFetchedAt = configurationFetchedAt; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
