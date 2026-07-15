package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.AccountRecovery;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_account_recovery.
 */
public interface AccountRecoveryRepository {

    void save(AccountRecovery recovery);

    void update(AccountRecovery recovery);

    Optional<AccountRecovery> findById(String id);

    List<AccountRecovery> findPending();
}
