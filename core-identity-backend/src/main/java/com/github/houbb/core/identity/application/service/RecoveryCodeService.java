package com.github.houbb.core.identity.application.service;

import java.util.List;

/**
 * Recovery code service — generates, validates, and manages recovery codes.
 */
public interface RecoveryCodeService {

    /**
     * Generate a new set of recovery codes for a user. Returns plaintext codes (one-time display).
     * Revokes any existing active set.
     */
    List<String> generate(String userId);

    /**
     * Validate and consume a recovery code. Returns true if valid.
     */
    boolean verify(String userId, String code);

    /**
     * Get the current status of recovery codes for a user.
     */
    RecoveryCodeStatus getStatus(String userId);

    /**
     * Regenerate recovery codes (revokes old, generates new). Requires step-up auth.
     */
    List<String> regenerate(String userId);

    record RecoveryCodeStatus(int remainingCount, int totalCount, long generatedAt, boolean isActive) {}
}
