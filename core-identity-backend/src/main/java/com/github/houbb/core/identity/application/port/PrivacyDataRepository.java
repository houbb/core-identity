package com.github.houbb.core.identity.application.port;

import java.util.List;
import java.util.Map;

/**
 * Repository for privacy tables: identity_privacy_request, identity_privacy_request_task,
 * identity_retention_policy, identity_legal_hold, identity_legal_hold_scope, identity_processing_activity.
 */
public interface PrivacyDataRepository {

    // Privacy Request
    void insertRequest(String id, String userId, String organizationId, String requestType,
                       String jurisdiction, long submittedAt, long dueAt, long now);

    void updateRequestStatus(String id, String status, String verificationLevel, long now);

    void updateRequestCompleted(String id, String status, long now);

    void updateRequestRejected(String id, String reason, long now);

    List<Map<String, Object>> findRequestsByStatus(String status);

    Map<String, Object> findRequestById(String requestId);

    List<Map<String, Object>> findTasksByRequestId(String requestId);

    // Tasks
    void insertTask(String id, String privacyRequestId, String targetService, String taskType, long now);

    // Retention Policy
    void insertRetentionPolicy(String id, String organizationId, String dataCategory, String triggerType,
                               long retentionSeconds, String expirationAction, String jurisdiction,
                               int priority, long now);

    List<Map<String, Object>> findRetentionPoliciesByOrg(String organizationId);

    // Legal Hold
    void insertLegalHold(String id, String organizationId, String caseReference, String name,
                         String reason, long effectiveAt, long reviewAt, String createdBy, long now);

    void insertLegalHoldScope(String id, String legalHoldId, String scopeType, String scopeReference, String dataCategory);

    void releaseLegalHold(String id, long now);

    List<Map<String, Object>> findLegalHoldsByOrg(String organizationId);

    int countActiveLegalHoldByScope(String scopeReference, String dataCategory);

    // Processing Activity
    void insertProcessingActivity(String id, String organizationId, String activityCode, String name,
                                   String purpose, String controller, long now);

    List<Map<String, Object>> findProcessingActivitiesByOrg(String organizationId);
}