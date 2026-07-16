package com.github.houbb.core.identity.application.domain;

/**
 * Event subscription for outbox relay (P7.3).
 * <p>
 * Defines how outbox events are delivered to external HTTP subscribers.
 */
public class EventSubscription {

    private String id;
    private String subscriberType;     // WEBHOOK, INTERNAL
    private String subscriberId;
    private String eventPattern;       // e.g. "identity.user.*" or "identity.*"
    private String endpoint;           // HTTP URL for webhook delivery
    private String signingSecretReference; // key reference for HMAC signing
    private String status;             // ACTIVE, DISABLED, FAILED
    private String retryPolicyJson;    // {"maxAttempts":5, "backoffMs":1000, "multiplier":2.0}
    private long createdAt;
    private long updatedAt;
    private long version;

    public EventSubscription() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSubscriberType() { return subscriberType; }
    public void setSubscriberType(String subscriberType) { this.subscriberType = subscriberType; }

    public String getSubscriberId() { return subscriberId; }
    public void setSubscriberId(String subscriberId) { this.subscriberId = subscriberId; }

    public String getEventPattern() { return eventPattern; }
    public void setEventPattern(String eventPattern) { this.eventPattern = eventPattern; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getSigningSecretReference() { return signingSecretReference; }
    public void setSigningSecretReference(String signingSecretReference) { this.signingSecretReference = signingSecretReference; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRetryPolicyJson() { return retryPolicyJson; }
    public void setRetryPolicyJson(String retryPolicyJson) { this.retryPolicyJson = retryPolicyJson; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
