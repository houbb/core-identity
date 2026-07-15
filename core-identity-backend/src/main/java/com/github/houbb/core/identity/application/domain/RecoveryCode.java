package com.github.houbb.core.identity.application.domain;

/**
 * Individual recovery code — only the hash is stored, never the plaintext.
 */
public class RecoveryCode {

    private String id;
    private String codeSetId;
    private String codeHash;
    private String status;
    private Long usedAt;
    private long createdAt;

    public RecoveryCode() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCodeSetId() { return codeSetId; }
    public void setCodeSetId(String codeSetId) { this.codeSetId = codeSetId; }
    public String getCodeHash() { return codeHash; }
    public void setCodeHash(String codeHash) { this.codeHash = codeHash; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getUsedAt() { return usedAt; }
    public void setUsedAt(Long usedAt) { this.usedAt = usedAt; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
