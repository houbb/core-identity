package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.port.PrivacyDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 隐私与数据生命周期服务 — 管理隐私请求、数据保留策略和法律保留。
 */
public class PrivacyService {

    private static final Logger log = LoggerFactory.getLogger(PrivacyService.class);

    private final PrivacyDataRepository repo;

    public PrivacyService(PrivacyDataRepository repo) {
        this.repo = repo;
    }

    // ========== Privacy Request ==========

    @Transactional
    public Map<String, Object> submitPrivacyRequest(String userId, String organizationId,
                                                     String requestType, String jurisdiction) {
        long now = System.currentTimeMillis();
        String id = UUID.randomUUID().toString();
        long dueAt = now + 30L * 24 * 3600 * 1000; // 默认 30 天

        repo.insertRequest(id, userId, organizationId, requestType, jurisdiction, now, dueAt, now);
        log.info("Privacy request submitted: id={}, user={}, type={}", id, userId, requestType);
        return Map.of("id", id, "userId", userId, "requestType", requestType, "status", "SUBMITTED", "dueAt", dueAt);
    }

    @Transactional
    public void verifyIdentity(String requestId, String verificationLevel) {
        long now = System.currentTimeMillis();
        repo.updateRequestStatus(requestId, "IDENTITY_VERIFICATION", verificationLevel, now);
    }

    @Transactional
    public void approvePrivacyRequest(String requestId) {
        long now = System.currentTimeMillis();
        repo.updateRequestStatus(requestId, "IN_PROGRESS", null, now);

        String taskId = UUID.randomUUID().toString();
        repo.insertTask(taskId, requestId, "core-identity", "DATA_EXPORT", now);
        log.info("Privacy request approved: id={}", requestId);
    }

    @Transactional
    public void completePrivacyRequest(String requestId, String status) {
        long now = System.currentTimeMillis();
        repo.updateRequestCompleted(requestId, status, now);
    }

    @Transactional
    public void rejectPrivacyRequest(String requestId, String reason) {
        long now = System.currentTimeMillis();
        repo.updateRequestRejected(requestId, reason, now);
    }

    public List<Map<String, Object>> listPrivacyRequests(String status) {
        return repo.findRequestsByStatus(status);
    }

    public Map<String, Object> getPrivacyRequestDetail(String requestId) {
        Map<String, Object> request = repo.findRequestById(requestId);
        List<Map<String, Object>> tasks = repo.findTasksByRequestId(requestId);
        request.put("tasks", tasks);
        return request;
    }

    // ========== Retention Policy ==========

    @Transactional
    public Map<String, Object> createRetentionPolicy(String organizationId, String dataCategory,
                                                      String triggerType, long retentionSeconds,
                                                      String expirationAction, String jurisdiction, int priority) {
        long now = System.currentTimeMillis();
        String id = UUID.randomUUID().toString();
        repo.insertRetentionPolicy(id, organizationId, dataCategory, triggerType,
                retentionSeconds, expirationAction, jurisdiction, priority, now);
        return Map.of("id", id, "dataCategory", dataCategory, "retentionSeconds", retentionSeconds);
    }

    public List<Map<String, Object>> listRetentionPolicies(String organizationId) {
        return repo.findRetentionPoliciesByOrg(organizationId);
    }

    // ========== Legal Hold ==========

    @Transactional
    public Map<String, Object> createLegalHold(String organizationId, String caseReference, String name,
                                                String reason, String createdBy, long reviewAt) {
        long now = System.currentTimeMillis();
        String id = UUID.randomUUID().toString();
        repo.insertLegalHold(id, organizationId, caseReference, name, reason, now, reviewAt, createdBy, now);
        return Map.of("id", id, "caseReference", caseReference, "status", "ACTIVE");
    }

    @Transactional
    public void addLegalHoldScope(String legalHoldId, String scopeType, String scopeReference,
                                   String dataCategory) {
        String id = UUID.randomUUID().toString();
        repo.insertLegalHoldScope(id, legalHoldId, scopeType, scopeReference, dataCategory);
    }

    @Transactional
    public void releaseLegalHold(String legalHoldId, String releasedBy) {
        long now = System.currentTimeMillis();
        repo.releaseLegalHold(legalHoldId, now);
    }

    public List<Map<String, Object>> listLegalHolds(String organizationId) {
        return repo.findLegalHoldsByOrg(organizationId);
    }

    // ========== Processing Activity ==========

    @Transactional
    public Map<String, Object> createProcessingActivity(String organizationId, String activityCode,
                                                         String name, String purpose, String controller) {
        long now = System.currentTimeMillis();
        String id = UUID.randomUUID().toString();
        repo.insertProcessingActivity(id, organizationId, activityCode, name, purpose, controller, now);
        return Map.of("id", id, "activityCode", activityCode, "status", "ACTIVE");
    }

    public List<Map<String, Object>> listProcessingActivities(String organizationId) {
        return repo.findProcessingActivitiesByOrg(organizationId);
    }

    // ========== Legal Hold 冲突检查 ==========

    public boolean hasActiveLegalHold(String targetId, String dataCategory) {
        return repo.countActiveLegalHoldByScope(targetId, dataCategory) > 0;
    }

    public static class ServiceException extends RuntimeException {
        private final String errorCode;
        public ServiceException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }
        public String getErrorCode() { return errorCode; }
    }
}
