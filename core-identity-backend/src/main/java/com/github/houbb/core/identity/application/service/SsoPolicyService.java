package com.github.houbb.core.identity.application.service;

/**
 * SSO Policy Service — manages SSO enforcement rules per organization.
 *
 * P5: Controls whether members must use enterprise SSO. Supports OPTIONAL,
 * REQUIRED_FOR_MEMBERS, REQUIRED_FOR_DOMAINS, and break-glass accounts.
 * Break-glass usage generates CRITICAL security events.
 */
public interface SsoPolicyService {

    record PolicyCheckResult(boolean ssoRequired, String reason, String connectionKey) {}

    /**
     * Check if SSO is required for a user accessing an organization.
     */
    PolicyCheckResult checkEnforcement(String organizationId, String userId);

    /**
     * Check if the given user is a break-glass account for this organization.
     */
    boolean isBreakGlassAccount(String organizationId, String userId);

    /**
     * Record break-glass usage (CRITICAL security event).
     */
    void recordBreakGlassUsage(String organizationId, String userId, String requestId, String sourceIp);
}
