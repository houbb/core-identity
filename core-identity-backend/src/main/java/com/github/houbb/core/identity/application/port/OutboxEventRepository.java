package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.OutboxEvent;

import java.util.List;

/**
 * Repository for outbox events.
 */
public interface OutboxEventRepository {

    void save(OutboxEvent event);

    List<OutboxEvent> findPendingEvents(int limit);

    void updateStatus(String eventId, String status, String lastError);

    void incrementAttempt(String eventId);
}