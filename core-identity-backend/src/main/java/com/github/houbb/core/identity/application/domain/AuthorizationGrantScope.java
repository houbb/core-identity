package com.github.houbb.core.identity.application.domain;

/**
 * Association between AuthorizationGrant and Scope.
 * Tracks which scopes were granted in a specific authorization.
 *
 * Table: identity_authorization_grant_scope
 */
public class AuthorizationGrantScope {

    private String id;
    private String grantId;
    private String scopeId;
    private long createdAt;

    public AuthorizationGrantScope() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getGrantId() { return grantId; }
    public void setGrantId(String grantId) { this.grantId = grantId; }
    public String getScopeId() { return scopeId; }
    public void setScopeId(String scopeId) { this.scopeId = scopeId; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}