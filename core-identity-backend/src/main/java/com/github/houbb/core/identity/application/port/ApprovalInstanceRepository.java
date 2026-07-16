package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.ApprovalInstance;

import java.util.Optional;

/**
 * Repository for identity_approval_instance.
 */
public interface ApprovalInstanceRepository {

    void save(ApprovalInstance instance);

    Optional<ApprovalInstance> findById(String id);

    Optional<ApprovalInstance> findByRequest(String requestType, String requestId);

    void update(ApprovalInstance instance);

    void updateStatus(String id, String status, long completedAt, long now, long version);
}
