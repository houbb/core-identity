package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.EventDeliveryAttempt;
import com.github.houbb.core.identity.application.domain.EventSubscription;
import com.github.houbb.core.identity.application.domain.OutboxEvent;
import com.github.houbb.core.identity.application.port.EventDeliveryAttemptRepository;
import com.github.houbb.core.identity.application.port.EventSubscriptionRepository;
import com.github.houbb.core.identity.application.port.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Outbox event relay service (P7.3).
 * <p>
 * Polls the outbox table for pending events and delivers them to
 * registered HTTP subscribers (webhooks). Each delivery attempt is
 * recorded. Failed deliveries are retried with exponential backoff.
 * <p>
 * This is an HTTP-only relay. No Kafka, no message broker.
 */
public class OutboxRelayService {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelayService.class);

    private static final int BATCH_SIZE = 50;
    private static final long INITIAL_BACKOFF_MS = 1000;
    private static final double BACKOFF_MULTIPLIER = 2.0;
    private static final int MAX_RETRY_ATTEMPTS = 5;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final OutboxEventRepository outboxRepo;
    private final EventSubscriptionRepository subscriptionRepo;
    private final EventDeliveryAttemptRepository deliveryRepo;
    private final boolean enabled;

    public OutboxRelayService(OutboxEventRepository outboxRepo,
                              EventSubscriptionRepository subscriptionRepo,
                              EventDeliveryAttemptRepository deliveryRepo,
                              boolean enabled) {
        this.outboxRepo = outboxRepo;
        this.subscriptionRepo = subscriptionRepo;
        this.deliveryRepo = deliveryRepo;
        this.enabled = enabled;
        if (enabled) {
            log.info("OutboxRelayService enabled — HTTP-based event relay (no Kafka)");
        } else {
            log.info("OutboxRelayService disabled (standalone mode)");
        }
    }

    /**
     * Called periodically (via @Scheduled or manual invocation).
     * Relays pending outbox events to registered subscribers.
     */
    public void relayPendingEvents() {
        if (!enabled) {
            return;
        }

        List<OutboxEvent> pendingEvents = outboxRepo.findPendingEvents(BATCH_SIZE);
        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Found {} pending outbox events to relay", pendingEvents.size());

        List<EventSubscription> activeSubscriptions = subscriptionRepo.findByStatus("ACTIVE");
        if (activeSubscriptions.isEmpty()) {
            log.debug("No active subscriptions — skipping relay");
            return;
        }

        for (OutboxEvent event : pendingEvents) {
            for (EventSubscription sub : activeSubscriptions) {
                if (matchesPattern(event.getEventType(), sub.getEventPattern())) {
                    deliverEvent(event, sub);
                }
            }
        }
    }

    /**
     * Check if an event type matches a subscription pattern.
     * Supports wildcards: "identity.user.*" matches "identity.user.registered".
     */
    private boolean matchesPattern(String eventType, String pattern) {
        if ("*".equals(pattern) || "identity.*".equals(pattern)) {
            return true;
        }
        if (pattern.endsWith(".*")) {
            String prefix = pattern.substring(0, pattern.length() - 2);
            return eventType.startsWith(prefix);
        }
        return pattern.equals(eventType);
    }

    /**
     * Deliver a single event to a single subscriber.
     */
    private void deliverEvent(OutboxEvent event, EventSubscription sub) {
        long now = System.currentTimeMillis();
        String attemptId = UUID.randomUUID().toString();

        EventDeliveryAttempt attempt = new EventDeliveryAttempt();
        attempt.setId(attemptId);
        attempt.setOutboxEventId(event.getId());
        attempt.setDestination(sub.getEndpoint());
        attempt.setAttemptNumber(event.getAttemptCount() + 1);
        attempt.setStatus("PENDING");
        attempt.setStartedAt(now);
        deliveryRepo.save(attempt);

        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(sub.getEndpoint()))
                    .header("Content-Type", "application/json")
                    .header("X-Event-Type", event.getEventType())
                    .header("X-Event-Id", event.getId())
                    .header("X-Event-Version", String.valueOf(event.getEventVersion()))
                    .timeout(REQUEST_TIMEOUT)
                    .POST(HttpRequest.BodyPublishers.ofString(event.getPayloadJson() != null ? event.getPayloadJson() : "{}"))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            long completedAt = System.currentTimeMillis();

            if (statusCode >= 200 && statusCode < 300) {
                // Success
                outboxRepo.updateStatus(event.getId(), "PUBLISHED", null);
                attempt.setStatus("SUCCESS");
                attempt.setResponseCode(statusCode);
                attempt.setCompletedAt(completedAt);
                deliveryRepo.update(attempt);
                log.debug("Event {} delivered to {} — HTTP {}", event.getId(), sub.getEndpoint(), statusCode);
            } else {
                // Subscriber returned error
                handleDeliveryFailure(event, attempt, sub, "HTTP " + statusCode, completedAt);
            }
        } catch (Exception e) {
            long completedAt = System.currentTimeMillis();
            handleDeliveryFailure(event, attempt, sub, e.getMessage(), completedAt);
        }
    }

    /**
     * Handle a failed delivery attempt with retry/back-off/dead-letter logic.
     */
    private void handleDeliveryFailure(OutboxEvent event, EventDeliveryAttempt attempt,
                                       EventSubscription sub, String error, long completedAt) {
        attempt.setStatus("FAILED");
        attempt.setErrorCode(error);
        attempt.setCompletedAt(completedAt);

        int attemptCount = event.getAttemptCount() + 1;
        outboxRepo.incrementAttempt(event.getId());

        if (attemptCount >= MAX_RETRY_ATTEMPTS) {
            // Dead letter
            outboxRepo.updateStatus(event.getId(), "DEAD_LETTER", error);
            attempt.setStatus("DEAD_LETTER");
            log.warn("Event {} dead-lettered after {} attempts: {}", event.getId(), attemptCount, error);
        } else {
            // Schedule retry with exponential backoff
            long backoffMs = (long) (INITIAL_BACKOFF_MS * Math.pow(BACKOFF_MULTIPLIER, attemptCount - 1));
            long nextAttempt = completedAt + backoffMs;
            outboxRepo.updateStatus(event.getId(), "RETRYING", error);
            attempt.setNextAttemptAt(nextAttempt);
            log.info("Event {} delivery failed (attempt {}/{}), retry in {}ms: {}",
                    event.getId(), attemptCount, MAX_RETRY_ATTEMPTS, backoffMs, error);
        }

        deliveryRepo.update(attempt);
    }

    /**
     * Replay a dead-lettered event.
     */
    public void replay(String eventId) {
        outboxRepo.updateStatus(eventId, "PENDING", null);
        log.info("Event {} reset to PENDING for replay", eventId);
    }

    public boolean isEnabled() {
        return enabled;
    }
}
