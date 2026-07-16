package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.ApprovalDecision;

import java.util.List;
import java.util.Optional;

/**
 * Repository for identity_approval_decision.
 */
public interface ApprovalDecisionRepository {

    void save(ApprovalDecision decision);

    Optional<ApprovalDecision> findById(String id);

    List<ApprovalDecision> findByStepId(String approvalStepId);

    List<ApprovalDecision> findByApproverId(String approverUserId);

    int countByStepIdAndDecision(String approvalStepId, String decision);
}
