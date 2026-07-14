package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.command.OutboxCommand;

/**
 * Outbox event service.
 */
public interface OutboxService {

    /**
     * Write an event to the outbox.
     *
     * @return the event ID
     */
    String write(OutboxCommand command);
}