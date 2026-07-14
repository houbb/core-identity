package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.command.IdempotencyCommand;
import com.github.houbb.core.identity.application.domain.IdempotencyRecord;
import com.github.houbb.core.identity.application.port.IdempotencyRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Default implementation of IdempotencyService.
 */
public class IdempotencyServiceImpl implements IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyServiceImpl.class);

    private final IdempotencyRecordRepository repository;
    private final int recordTtlHours;

    public IdempotencyServiceImpl(IdempotencyRecordRepository repository, int recordTtlHours) {
        this.repository = repository;
        this.recordTtlHours = recordTtlHours;
    }

    @Override
    public boolean checkOrCreate(IdempotencyCommand command) {
        Optional<IdempotencyRecord> existing = repository.findByKey(command.getScope(), command.getIdempotencyKey());

        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();
            if ("PROCESSING".equals(record.getStatus())) {
                // Still processing — reject duplicate
                return false;
            }
            // Already completed (SUCCEEDED/FAILED) — return true for idempotent replay
            return true;
        }

        // Create new record
        Instant now = Instant.now();
        IdempotencyRecord record = new IdempotencyRecord();
        record.setId(UUID.randomUUID().toString());
        record.setIdempotencyKey(command.getIdempotencyKey());
        record.setScope(command.getScope());
        record.setRequestHash(command.getRequestHash());
        record.setStatus("PROCESSING");
        record.setLockedUntil(now.plusSeconds(30).toEpochMilli());
        record.setExpiresAt(now.plusSeconds(recordTtlHours * 3600L).toEpochMilli());
        record.setCreatedAt(now.toEpochMilli());
        record.setUpdatedAt(now.toEpochMilli());

        repository.save(record);
        return true;
    }

    @Override
    public void markSucceeded(IdempotencyCommand command, int httpStatus, String responseBody) {
        Optional<IdempotencyRecord> existing = repository.findByKey(command.getScope(), command.getIdempotencyKey());
        existing.ifPresent(record -> {
            repository.updateStatus(record.getId(), "SUCCEEDED", httpStatus, responseBody);
        });
    }

    @Override
    public void markFailed(IdempotencyCommand command, int httpStatus, String responseBody) {
        Optional<IdempotencyRecord> existing = repository.findByKey(command.getScope(), command.getIdempotencyKey());
        existing.ifPresent(record -> {
            repository.updateStatus(record.getId(), "FAILED", httpStatus, responseBody);
        });
    }
}