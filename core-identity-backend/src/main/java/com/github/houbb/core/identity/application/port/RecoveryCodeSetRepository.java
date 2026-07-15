package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.RecoveryCode;
import com.github.houbb.core.identity.application.domain.RecoveryCodeSet;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_recovery_code_set.
 */
public interface RecoveryCodeSetRepository {

    void save(RecoveryCodeSet set);

    Optional<RecoveryCodeSet> findByUserIdAndStatus(String userId, String status);

    void updateStatus(String id, String status, long version);

    void decrementRemaining(String id, long version);

    void revokeByUserId(String userId, long revokedAt);
}
