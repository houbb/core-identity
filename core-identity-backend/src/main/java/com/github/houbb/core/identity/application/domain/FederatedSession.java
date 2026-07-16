package com.github.houbb.core.identity.application.domain;

/**
 * Federated Session domain object — links a Core Identity session to an upstream IdP session.
 *
 * P5: Enables single logout, audit trails, and authentication strength mapping.
 * Does not store upstream tokens.
 * Table: identity_federated_session
 */
public class FederatedSession {

    private String id;
    private String sessionId;
    private String connectionId;
    private String externalIdentityId;
    private String upstreamSessionId;
    private String upstreamSubject;
    private Long upstreamAuthTime;
    private String upstreamAcr;
    private String upstreamAmrJson;
    private String logoutStatus;
    private long createdAt;
    private long updatedAt;
    private long version;

    public FederatedSession() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getConnectionId() { return connectionId; }
    public void setConnectionId(String connectionId) { this.connectionId = connectionId; }
    public String getExternalIdentityId() { return externalIdentityId; }
    public void setExternalIdentityId(String externalIdentityId) { this.externalIdentityId = externalIdentityId; }
    public String getUpstreamSessionId() { return upstreamSessionId; }
    public void setUpstreamSessionId(String upstreamSessionId) { this.upstreamSessionId = upstreamSessionId; }
    public String getUpstreamSubject() { return upstreamSubject; }
    public void setUpstreamSubject(String upstreamSubject) { this.upstreamSubject = upstreamSubject; }
    public Long getUpstreamAuthTime() { return upstreamAuthTime; }
    public void setUpstreamAuthTime(Long upstreamAuthTime) { this.upstreamAuthTime = upstreamAuthTime; }
    public String getUpstreamAcr() { return upstreamAcr; }
    public void setUpstreamAcr(String upstreamAcr) { this.upstreamAcr = upstreamAcr; }
    public String getUpstreamAmrJson() { return upstreamAmrJson; }
    public void setUpstreamAmrJson(String upstreamAmrJson) { this.upstreamAmrJson = upstreamAmrJson; }
    public String getLogoutStatus() { return logoutStatus; }
    public void setLogoutStatus(String logoutStatus) { this.logoutStatus = logoutStatus; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
