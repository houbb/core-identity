package com.github.houbb.core.identity.application.domain;

/**
 * OAuth Scope — a named capability that a Client or credential may request.
 *
 * Table: identity_scope
 */
public class Scope {

    private String id;
    private String scopeCode;
    private String sourceService;
    private String audienceCode;
    private String name;
    private String description;
    private String riskLevel;
    private String consentDisplay;
    private int assignable;
    private String status;
    private long createdAt;
    private long updatedAt;
    private long version;

    public Scope() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getScopeCode() { return scopeCode; }
    public void setScopeCode(String scopeCode) { this.scopeCode = scopeCode; }
    public String getSourceService() { return sourceService; }
    public void setSourceService(String sourceService) { this.sourceService = sourceService; }
    public String getAudienceCode() { return audienceCode; }
    public void setAudienceCode(String audienceCode) { this.audienceCode = audienceCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getConsentDisplay() { return consentDisplay; }
    public void setConsentDisplay(String consentDisplay) { this.consentDisplay = consentDisplay; }
    public int getAssignable() { return assignable; }
    public void setAssignable(int assignable) { this.assignable = assignable; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}