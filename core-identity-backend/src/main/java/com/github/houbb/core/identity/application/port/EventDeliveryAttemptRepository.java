package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.EventDeliveryAttempt;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_event_delivery_attempt (P7.3).
 */
public interface EventDeliveryAttemptRepository {

    void save(EventDeliveryAttempt attempt);

    Optional<EventDeliveryAttempt> findById(String id);

    List<EventDeliveryAttempt> findByOutboxEventId(String outboxEventId);

    void update(EventDeliveryAttempt attempt);

    List<EventDeliveryAttempt> findByStatus(String status);
}
