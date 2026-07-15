package com.github.houbb.core.identity.application.domain;

/**
 * Security event domain object — security-related events for monitoring and audit.
 */
public class SecurityEvent {

    private String id;
    private String userId;
    private String organizationId;
    private String eventType;
    private String severity;
    private String status;
    private String source;
    private String riskAssessmentId;
    private String title;
    private String description;
    private String metadataJson;
    private long detectedAt;
    private Long resolvedAt;
    private String resolvedBy;
    private String resolution;
    private long createdAt;
    private long updatedAt;
    private long version;

    public SecurityEvent() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getRiskAssessmentId() { return riskAssessmentId; }
    public void setRiskAssessmentId(String riskAssessmentId) { this.riskAssessmentId = riskAssessmentId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }
    public long getDetectedAt() { return detectedAt; }
    public void setDetectedAt(long detectedAt) { this.detectedAt = detectedAt; }
    public Long getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Long resolvedAt) { this.resolvedAt = resolvedAt; }
    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
