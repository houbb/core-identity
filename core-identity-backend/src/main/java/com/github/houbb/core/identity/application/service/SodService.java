package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.SodPolicy;
import com.github.houbb.core.identity.application.port.SodDataRepository;
import com.github.houbb.core.identity.application.port.SodPolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 职责分离 (SoD) 服务 — 管理冲突策略、检测冲突、处理例外。
 */
public class SodService {

    private static final Logger log = LoggerFactory.getLogger(SodService.class);

    private final SodPolicyRepository policyRepo;
    private final SodDataRepository dataRepo;

    public SodService(SodPolicyRepository policyRepo, SodDataRepository dataRepo) {
        this.policyRepo = policyRepo;
        this.dataRepo = dataRepo;
    }

    // ========== Policy ==========

    @Transactional
    public SodPolicy createPolicy(String organizationId, String name, String enforcementMode,
                                   String ownerUserId) {
        long now = System.currentTimeMillis();
        SodPolicy policy = new SodPolicy();
        policy.setId(UUID.randomUUID().toString());
        policy.setOrganizationId(organizationId);
        policy.setName(name);
        policy.setPolicyType("STATIC");
        policy.setEnforcementMode(enforcementMode != null ? enforcementMode : "DENY");
        policy.setStatus("ACTIVE");
        policy.setOwnerUserId(ownerUserId);
        policy.setCreatedAt(now);
        policy.setUpdatedAt(now);
        policy.setVersion(1);
        policyRepo.save(policy);
        log.info("Created SoD policy: {} in org {}", name, organizationId);
        return policy;
    }

    @Transactional
    public void addPolicyItem(String policyId, String leftEntitlementId, String rightEntitlementId,
                               String riskLevel) {
        String id = UUID.randomUUID().toString();
        dataRepo.insertPolicyItem(id, policyId, leftEntitlementId, rightEntitlementId,
                riskLevel != null ? riskLevel : "HIGH");
    }

    @Transactional
    public SodConflict detectConflict(String subjectId) {
        List<Map<String, Object>> grants = dataRepo.findActiveGrantsBySubject(subjectId);
        List<Map<String, Object>> policyItems = dataRepo.findPolicyItemsByStatus("ACTIVE");

        long now = System.currentTimeMillis();
        for (Map<String, Object> item : policyItems) {
            String leftEntId = (String) item.get("left_entitlement_id");
            String rightEntId = (String) item.get("right_entitlement_id");
            String policyId = (String) item.get("policy_id");

            boolean hasLeft = grants.stream().anyMatch(g -> leftEntId.equals(g.get("entitlement_id")));
            boolean hasRight = grants.stream().anyMatch(g -> rightEntId.equals(g.get("entitlement_id")));

            if (hasLeft && hasRight) {
                Map<String, Object> leftGrant = grants.stream()
                        .filter(g -> leftEntId.equals(g.get("entitlement_id"))).findFirst().orElse(null);
                Map<String, Object> rightGrant = grants.stream()
                        .filter(g -> rightEntId.equals(g.get("entitlement_id"))).findFirst().orElse(null);

                List<Map<String, Object>> existing = dataRepo.findOpenConflictsByPolicyAndSubject(subjectId, policyId);
                if (!existing.isEmpty()) {
                    continue;
                }

                String conflictId = UUID.randomUUID().toString();
                dataRepo.insertConflict(conflictId, policyId, subjectId,
                        leftGrant != null ? (String) leftGrant.get("id") : "",
                        rightGrant != null ? (String) rightGrant.get("id") : "",
                        now);

                return new SodConflict(conflictId, policyId, subjectId, "OPEN",
                        (String) item.get("enforcement_mode"));
            }
        }
        return new SodConflict(null, null, subjectId, "NO_CONFLICT", null);
    }

    @Transactional
    public void createException(String conflictId, String reason, String compensatingControl,
                                 String approvedBy, long durationSeconds) {
        long now = System.currentTimeMillis();
        String id = UUID.randomUUID().toString();
        dataRepo.insertException(id, conflictId, reason, compensatingControl, approvedBy, now,
                now + durationSeconds * 1000);
        dataRepo.updateConflictStatus(conflictId, "EXCEPTION_GRANTED", "EXCEPTION", now);
    }

    public List<SodPolicy> listPolicies(String organizationId) {
        return policyRepo.findByOrgId(organizationId);
    }

    public List<Map<String, Object>> listConflicts(String organizationId) {
        return dataRepo.findConflictsByOrg(organizationId);
    }

    @Transactional
    public void resolveConflict(String conflictId, String resolution) {
        long now = System.currentTimeMillis();
        dataRepo.updateConflictStatus(conflictId, "RESOLVED", resolution, now);
    }

    public static class ServiceException extends RuntimeException {
        private final String errorCode;
        public ServiceException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }
        public String getErrorCode() { return errorCode; }
    }

    public record SodConflict(String id, String policyId, String subjectId, String status, String enforcementMode) {}
}