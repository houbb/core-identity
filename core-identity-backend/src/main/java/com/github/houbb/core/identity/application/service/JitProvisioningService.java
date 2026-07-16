package com.github.houbb.core.identity.application.service;

/**
 * JIT (Just-In-Time) Provisioning Service — auto-creates users and memberships on first SSO login.
 *
 * P5: Never assigns OWNER role. Respects SCIM lifecycle priority (SCIM-deactivated users
 * cannot be re-activated by JIT). Checks domain allowlists and seat limits.
 */
public interface JitProvisioningService {

    record JitProvisionResult(String userId, String membershipId, boolean created, String message) {}

    /**
     * Provision a user on first SSO login. Creates User + UserEmail + Membership + ExternalIdentity
     * in a single transaction if the JIT policy allows it.
     */
    JitProvisionResult provisionIfNeeded(String connectionId, String externalSubject, String email,
                                         boolean emailVerified, String displayName,
                                         String organizationId, long now);

    /**
     * Check whether JIT provisioning is allowed for the given connection and email domain.
     */
    boolean isJitAllowed(String connectionId, String email);
}
