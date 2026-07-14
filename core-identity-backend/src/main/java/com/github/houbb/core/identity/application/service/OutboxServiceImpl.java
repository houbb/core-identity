package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.command.OutboxCommand;
import com.github.houbb.core.identity.application.domain.OutboxEvent;
import com.github.houbb.core.identity.application.port.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

/**
 * Default implementation of OutboxService.
 */
public class OutboxServiceImpl implements OutboxService {

    private static final Logger log = LoggerFactory.getLogger(OutboxServiceImpl.class);

    private final OutboxEventRepository repository;

    public OutboxServiceImpl(OutboxEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public String write(OutboxCommand command) {
        Instant now = Instant.now();
        OutboxEvent event = new OutboxEvent();
        event.setId(UUID.randomUUID().toString());
        event.setEventType(command.getEventType());
        event.setEventVersion(command.getEventVersion());
        event.setAggregateType(command.getAggregateType());
        event.setAggregateId(command.getAggregateId());
        event.setPayloadJson(command.getPayloadJson());
        event.setHeadersJson(command.getHeadersJson());
        event.setStatus("PENDING");
        event.setAttemptCount(0);
        event.setNextAttemptAt(now.toEpochMilli());
        event.setCreatedAt(now.toEpochMilli());
        event.setUpdatedAt(now.toEpochMilli());
        event.setVersion(1);

        repository.save(event);
        log.debug("Outbox event written: {} - {}", event.getEventType(), event.getId());
        return event.getId();
    }
}