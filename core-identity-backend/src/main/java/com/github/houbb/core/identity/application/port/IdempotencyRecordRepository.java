package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.IdempotencyRecord;

import java.util.Optional;

/**
 * Repository for idempotency records.
 */
public interface IdempotencyRecordRepository {

    void save(IdempotencyRecord record);

    Optional<IdempotencyRecord> findByKey(String scope, String idempotencyKey);

    void updateStatus(String id, String status, Integer responseStatus, String responseBody);

    void deleteExpired(long beforeTimestamp);
}