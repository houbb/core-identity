package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.RecoveryCode;

import java.util.List;

/**
 * Repository for identity_recovery_code.
 */
public interface RecoveryCodeRepository {

    void save(RecoveryCode code);

    void saveBatch(List<RecoveryCode> codes);

    RecoveryCode findByCodeHash(String codeHash);

    void markUsed(String id, long usedAt);

    void markAllRevokedByCodeSetId(String codeSetId);
}
