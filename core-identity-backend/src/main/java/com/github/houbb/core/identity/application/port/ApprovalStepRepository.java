package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.ApprovalStep;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_approval_step.
 */
public interface ApprovalStepRepository {

    void save(ApprovalStep step);

    void saveBatch(List<ApprovalStep> steps);

    Optional<ApprovalStep> findById(String id);

    List<ApprovalStep> findByApprovalInstanceId(String approvalInstanceId);

    List<ApprovalStep> findByApprovalInstanceIdAndStatus(String approvalInstanceId, String status);

    void update(ApprovalStep step);

    void updateStatus(String id, String status, long now);

    void deleteByApprovalInstanceId(String approvalInstanceId);
}
