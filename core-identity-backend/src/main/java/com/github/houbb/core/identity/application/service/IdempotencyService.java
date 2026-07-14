package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.command.IdempotencyCommand;

/**
 * Idempotency service.
 */
public interface IdempotencyService {

    /**
     * Check or create idempotency record.
     *
     * @return true if the request is new (should proceed), false if duplicate
     */
    boolean checkOrCreate(IdempotencyCommand command);

    /**
     * Mark an idempotency record as succeeded.
     */
    void markSucceeded(IdempotencyCommand command, int httpStatus, String responseBody);

    /**
     * Mark an idempotency record as failed.
     */
    void markFailed(IdempotencyCommand command, int httpStatus, String responseBody);
}