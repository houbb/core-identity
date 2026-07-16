package com.github.houbb.core.identity.application.domain;

/**
 * JIT Policy domain object — Just-In-Time provisioning rules for a federation connection.
 *
 * P5: Controls auto-creation of users/memberships on first SSO login.
 * Default roles are always MEMBER/VIEWER — never OWNER.
 * Table: identity_jit_policy
 */
public class JitPolicy {

    private String id;
    private String connectionId;
    private String status;
    private int allowNewUsers;
    private int allowExistingLink;
    private int requireVerifiedEmail;
    private String allowedDomainsJson;
    private String defaultRoleIdsJson;
    private int syncProfileOnLogin;
    private int syncGroupsOnLogin;
    private int requireApproval;
    private long createdAt;
    private long updatedAt;
    private long version;

    public JitPolicy() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getConnectionId() { return connectionId; }
    public void setConnectionId(String connectionId) { this.connectionId = connectionId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getAllowNewUsers() { return allowNewUsers; }
    public void setAllowNewUsers(int allowNewUsers) { this.allowNewUsers = allowNewUsers; }
    public int getAllowExistingLink() { return allowExistingLink; }
    public void setAllowExistingLink(int allowExistingLink) { this.allowExistingLink = allowExistingLink; }
    public int getRequireVerifiedEmail() { return requireVerifiedEmail; }
    public void setRequireVerifiedEmail(int requireVerifiedEmail) { this.requireVerifiedEmail = requireVerifiedEmail; }
    public String getAllowedDomainsJson() { return allowedDomainsJson; }
    public void setAllowedDomainsJson(String allowedDomainsJson) { this.allowedDomainsJson = allowedDomainsJson; }
    public String getDefaultRoleIdsJson() { return defaultRoleIdsJson; }
    public void setDefaultRoleIdsJson(String defaultRoleIdsJson) { this.defaultRoleIdsJson = defaultRoleIdsJson; }
    public int getSyncProfileOnLogin() { return syncProfileOnLogin; }
    public void setSyncProfileOnLogin(int syncProfileOnLogin) { this.syncProfileOnLogin = syncProfileOnLogin; }
    public int getSyncGroupsOnLogin() { return syncGroupsOnLogin; }
    public void setSyncGroupsOnLogin(int syncGroupsOnLogin) { this.syncGroupsOnLogin = syncGroupsOnLogin; }
    public int getRequireApproval() { return requireApproval; }
    public void setRequireApproval(int requireApproval) { this.requireApproval = requireApproval; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
