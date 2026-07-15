package com.github.houbb.core.identity.application.domain;

/**
 * Risk assessment domain object — result of evaluating login risks.
 */
public class RiskAssessment {

    private String id;
    private String userId;
    private String sessionId;
    private String operation;
    private String riskLevel;
    private String decision;
    private String requiredAuthLevel;
    private int score;
    private String reasonsJson;
    private String modelVersion;
    private String requestId;
    private long createdAt;

    public RiskAssessment() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getRequiredAuthLevel() { return requiredAuthLevel; }
    public void setRequiredAuthLevel(String requiredAuthLevel) { this.requiredAuthLevel = requiredAuthLevel; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getReasonsJson() { return reasonsJson; }
    public void setReasonsJson(String reasonsJson) { this.reasonsJson = reasonsJson; }
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
