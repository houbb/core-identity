package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.RiskAssessment;
import com.github.houbb.core.identity.application.domain.SecurityEvent;
import com.github.houbb.core.identity.application.port.SecurityEventRepository;

import java.util.*;

/**
 * Risk engine — rule-based risk assessment for login and sensitive operations.
 * Outputs machine-readable reasons and decisions.
 */
public class RiskEngine {

    private final SecurityEventRepository securityEventRepo;
    private final int mediumThreshold;
    private final int highThreshold;
    private final int criticalThreshold;

    public RiskEngine(SecurityEventRepository securityEventRepo,
                      int mediumThreshold, int highThreshold, int criticalThreshold) {
        this.securityEventRepo = securityEventRepo;
        this.mediumThreshold = mediumThreshold;
        this.highThreshold = highThreshold;
        this.criticalThreshold = criticalThreshold;
    }

    /**
     * Assess risk for a login attempt. Returns the decision.
     */
    public RiskResult assess(RiskContext ctx) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        // New device
        if (ctx.newDevice()) {
            score += 20;
            reasons.add("NEW_DEVICE");
        }

        // New IP / location change
        if (ctx.ipChanged()) {
            score += 15;
            reasons.add("IP_CHANGED");
        }

        // Rapid IP changes (possible proxy/VPN hopping)
        if (ctx.rapidIpChanges()) {
            score += 25;
            reasons.add("RAPID_IP_CHANGES");
        }

        // Multiple password failures recently
        if (ctx.recentFailures() >= 3) {
            score += 25;
            reasons.add("MULTIPLE_FAILURES");
        }
        if (ctx.recentFailures() >= 10) {
            score += 20;
            reasons.add("PASSWORD_SPRAY_SUSPECTED");
        }

        // Privileged account bonus
        if (ctx.privilegedAccount()) {
            score += 20;
            reasons.add("PRIVILEGED_ACCOUNT");
        }

        // Unusual login time
        if (ctx.unusualTime()) {
            score += 10;
            reasons.add("UNUSUAL_TIME");
        }

        // Credential stuffing indicators
        if (ctx.credentialStuffingSuspected()) {
            score += 30;
            reasons.add("CREDENTIAL_STUFFING_SUSPECTED");
        }

        // Refresh token reuse
        if (ctx.refreshTokenReplaySuspected()) {
            score += 90;
            reasons.add("REFRESH_TOKEN_REUSE_DETECTED");
        }

        String riskLevel;
        String decision;
        String requiredAuthLevel;

        if (score >= criticalThreshold) {
            riskLevel = "CRITICAL";
            decision = "BLOCK";
            requiredAuthLevel = null;

            // Create security event for critical risks
            createSecurityEvent(ctx.userId(), "LOGIN_BLOCKED", "CRITICAL", reasons);
        } else if (score >= highThreshold) {
            riskLevel = "HIGH";
            decision = "STEP_UP_REQUIRED";
            requiredAuthLevel = "AUTH_LEVEL_2";

            if (ctx.privilegedAccount()) {
                createSecurityEvent(ctx.userId(), "LOGIN_HIGH_RISK", "HIGH", reasons);
            }
        } else if (score >= mediumThreshold) {
            riskLevel = "MEDIUM";
            decision = "ALLOW_WITH_MFA";
            requiredAuthLevel = "AUTH_LEVEL_2";
        } else {
            riskLevel = "LOW";
            decision = "ALLOW";
            requiredAuthLevel = null;
        }

        return new RiskResult(riskLevel, decision, requiredAuthLevel, score, reasons);
    }

    private void createSecurityEvent(String userId, String eventType, String severity, List<String> reasons) {
        try {
            long now = System.currentTimeMillis();
            SecurityEvent event = new SecurityEvent();
            event.setId(UUID.randomUUID().toString());
            event.setUserId(userId);
            event.setEventType(eventType);
            event.setSeverity(severity);
            event.setStatus("OPEN");
            event.setSource("RISK_ENGINE");
            event.setTitle(String.join(", ", reasons));
            event.setDescription("Risk score triggered: " + reasons);
            event.setDetectedAt(now);
            event.setCreatedAt(now);
            event.setUpdatedAt(now);
            event.setVersion(1);
            securityEventRepo.save(event);
        } catch (Exception ignored) {
            // Best-effort security event creation
        }
    }

    public record RiskContext(String userId, boolean newDevice, boolean ipChanged, boolean rapidIpChanges,
                              int recentFailures, boolean privilegedAccount, boolean unusualTime,
                              boolean credentialStuffingSuspected, boolean refreshTokenReplaySuspected) {}

    public record RiskResult(String riskLevel, String decision, String requiredAuthLevel, int score,
                             List<String> reasons) {}
}
