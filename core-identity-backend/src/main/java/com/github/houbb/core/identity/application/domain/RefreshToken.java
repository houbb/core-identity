package com.github.houbb.core.identity.application.domain;

public class RefreshToken {
    private String id; private String familyId; private String tokenHash; private String status;
    private long expiresAt; private Long usedAt; private String replacedById; private long createdAt; private long version;
    public RefreshToken() {}
    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getFamilyId() { return familyId; } public void setFamilyId(String familyId) { this.familyId = familyId; }
    public String getTokenHash() { return tokenHash; } public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public long getExpiresAt() { return expiresAt; } public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }
    public Long getUsedAt() { return usedAt; } public void setUsedAt(Long usedAt) { this.usedAt = usedAt; }
    public String getReplacedById() { return replacedById; } public void setReplacedById(String replacedById) { this.replacedById = replacedById; }
    public long getCreatedAt() { return createdAt; } public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getVersion() { return version; } public void setVersion(long version) { this.version = version; }
}