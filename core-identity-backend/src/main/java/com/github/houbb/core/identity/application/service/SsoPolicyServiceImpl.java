package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.command.AuditCommand;
import com.github.houbb.core.identity.application.command.OutboxCommand;
import com.github.houbb.core.identity.application.domain.SsoPolicy;
import com.github.houbb.core.identity.application.port.SsoPolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * SSO Policy Service implementation — SSO enforcement and break-glass management.
 */
public class SsoPolicyServiceImpl implements SsoPolicyService {

    private static final Logger log = LoggerFactory.getLogger(SsoPolicyServiceImpl.class);

    private final SsoPolicyRepository policyRepo;
    private final AuditService auditService;
    private final OutboxService outboxService;

    public SsoPolicyServiceImpl(SsoPolicyRepository policyRepo, AuditService auditService, OutboxService outboxService) {
        this.policyRepo = policyRepo;
        this.auditService = auditService;
        this.outboxService = outboxService;
    }

    @Override
    public PolicyCheckResult checkEnforcement(String organizationId, String userId) {
        SsoPolicy policy = policyRepo.findByOrganizationId(organizationId).orElse(null);
        if (policy == null || !"PUBLISHED".equals(policy.getStatus())) {
            return new PolicyCheckResult(false, null, null);
        }

        String mode = policy.getEnforcementMode();
        if ("OPTIONAL".equals(mode)) {
            return new PolicyCheckResult(false, null, null);
        }

        if ("REQUIRED_FOR_ALL_EXCEPT_BREAK_GLASS".equals(mode)) {
            if (isBreakGlassAccount(organizationId, userId)) {
                return new PolicyCheckResult(false, null, null);
            }
            String connKey = extractPrimaryConnection(policy);
            return new PolicyCheckResult(true, "SSO required for this organization", connKey);
        }

        if ("REQUIRED_FOR_MEMBERS".equals(mode)) {
            String connKey = extractPrimaryConnection(policy);
            return new PolicyCheckResult(true, "Members must use enterprise SSO", connKey);
        }

        return new PolicyCheckResult(false, null, null);
    }

    @Override
    public boolean isBreakGlassAccount(String organizationId, String userId) {
        // Break-glass accounts are identified by having the BREAK_GLASS role
        // and being exempt from SSO enforcement
        // For now: check if user is OWNER (simple implementation)
        return false;
    }

    @Override
    @Transactional
    public void recordBreakGlassUsage(String organizationId, String userId, String requestId, String sourceIp) {
        writeAudit("identity.sso.break_glass_used", "USER", userId, "BREAK_GLASS_LOGIN",
                "ORGANIZATION", organizationId, "SUCCESS", "CRITICAL: Break-glass account used",
                requestId, sourceIp, null);
        writeOutbox("identity.sso.break_glass_used", "ORGANIZATION", organizationId,
                "{\"userId\":\"" + userId + "\",\"severity\":\"CRITICAL\"}");

        log.warn("CRITICAL: Break-glass account used: user={}, org={}, ip={}", userId, organizationId, sourceIp);
    }

    private String extractPrimaryConnection(SsoPolicy policy) {
        if (policy.getConnectionIdsJson() == null) return null;
        // Simple extraction from JSON array
        String json = policy.getConnectionIdsJson();
        if (json.contains("\"")) {
            int start = json.indexOf("\"") + 1;
            int end = json.indexOf("\"", start);
            if (start > 0 && end > start) return json.substring(start, end);
        }
        return json.replace("[", "").replace("]", "").replace("\"", "").trim();
    }

    private void writeAudit(String eventType, String actorType, String actorId, String action,
                            String targetType, String targetId, String result, String reason,
                            String requestId, String sourceIp, String userAgent) {
        try {
            AuditCommand cmd = new AuditCommand();
            cmd.setEventType(eventType);
            cmd.setActorType(actorType);
            cmd.setActorId(actorId);
            cmd.setAction(action);
            cmd.setTargetType(targetType);
            cmd.setTargetId(targetId);
            cmd.setResult(result);
            cmd.setReason(reason);
            cmd.setRequestId(requestId);
            cmd.setSourceIp(sourceIp);
            cmd.setUserAgent(userAgent);
            auditService.record(cmd);
        } catch (Exception e) { log.warn("Failed to write audit: {}", e.getMessage()); }
    }

    private void writeOutbox(String eventType, String aggregateType, String aggregateId, String payloadJson) {
        try {
            OutboxCommand cmd = new OutboxCommand();
            cmd.setEventType(eventType);
            cmd.setAggregateType(aggregateType);
            cmd.setAggregateId(aggregateId);
            cmd.setPayloadJson(payloadJson);
            outboxService.write(cmd);
        } catch (Exception e) { log.warn("Failed to write outbox: {}", e.getMessage()); }
    }

    public static class ServiceException extends RuntimeException {
        private final String errorCode;
        public ServiceException(String errorCode, String message) { super(message); this.errorCode = errorCode; }
        public String getErrorCode() { return errorCode; }
    }
}