package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.command.AuditCommand;
import com.github.houbb.core.identity.application.domain.AuditEvent;
import com.github.houbb.core.identity.application.port.AuditEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

/**
 * Default implementation of AuditService.
 */
public class AuditServiceImpl implements AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditServiceImpl.class);

    private final AuditEventRepository repository;

    public AuditServiceImpl(AuditEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public String record(AuditCommand command) {
        AuditEvent event = new AuditEvent();
        event.setId(UUID.randomUUID().toString());
        event.setEventType(command.getEventType());
        event.setActorType(command.getActorType());
        event.setActorId(command.getActorId());
        event.setAction(command.getAction());
        event.setTargetType(command.getTargetType());
        event.setTargetId(command.getTargetId());
        event.setResult(command.getResult() != null ? command.getResult() : "SUCCESS");
        event.setReason(command.getReason());
        event.setRequestId(command.getRequestId());
        event.setSourceService(command.getSourceService());
        event.setSourceIp(command.getSourceIp());
        event.setUserAgent(command.getUserAgent());
        event.setMetadataJson(command.getMetadataJson());
        event.setOccurredAt(Instant.now().toEpochMilli());

        repository.save(event);
        log.debug("Audit event recorded: {} - {}", event.getEventType(), event.getId());
        return event.getId();
    }
}