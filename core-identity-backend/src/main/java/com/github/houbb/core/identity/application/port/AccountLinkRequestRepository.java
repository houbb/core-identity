package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.AccountLinkRequest;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_account_link_request.
 */
public interface AccountLinkRequestRepository {
    void save(AccountLinkRequest request);
    Optional<AccountLinkRequest> findById(String id);
    List<AccountLinkRequest> findByConnectionIdAndStatus(String connectionId, String status);
    List<AccountLinkRequest> findByStatus(String status);
    void update(AccountLinkRequest request);
    void updateStatus(String id, String status, long now, long version);
}
