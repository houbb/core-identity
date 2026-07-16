package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.EventSubscription;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_event_subscription (P7.3).
 */
public interface EventSubscriptionRepository {

    void save(EventSubscription subscription);

    Optional<EventSubscription> findById(String id);

    List<EventSubscription> findByStatus(String status);

    List<EventSubscription> findAll();

    void updateStatus(String id, String status, long updatedAt, long version);

    void delete(String id);
}
