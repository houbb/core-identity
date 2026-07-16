package com.github.houbb.core.identity.application.domain;

/**
 * Attribute Mapping domain object — maps external IdP claims/attributes to Core Identity fields.
 *
 * P5: Each mapping defines target attribute, source claim, ownership (LOCAL/JIT/SCIM/EXTERNAL_ALWAYS),
 * and optional transformation rules.
 * Table: identity_attribute_mapping
 */
public class AttributeMapping {

    private String id;
    private String connectionId;
    private String targetAttribute;
    private String sourceAttribute;
    private String sourceType;
    private String ownership;
    private int required;
    private String defaultValue;
    private String transformationType;
    private String status;
    private long createdAt;
    private long updatedAt;
    private long version;

    public AttributeMapping() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getConnectionId() { return connectionId; }
    public void setConnectionId(String connectionId) { this.connectionId = connectionId; }
    public String getTargetAttribute() { return targetAttribute; }
    public void setTargetAttribute(String targetAttribute) { this.targetAttribute = targetAttribute; }
    public String getSourceAttribute() { return sourceAttribute; }
    public void setSourceAttribute(String sourceAttribute) { this.sourceAttribute = sourceAttribute; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getOwnership() { return ownership; }
    public void setOwnership(String ownership) { this.ownership = ownership; }
    public int getRequired() { return required; }
    public void setRequired(int required) { this.required = required; }
    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
    public String getTransformationType() { return transformationType; }
    public void setTransformationType(String transformationType) { this.transformationType = transformationType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
