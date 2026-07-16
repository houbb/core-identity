package com.github.houbb.core.identity.application.service;

/**
 * Federation Session Service — manages federated session tracking and upstream IdP session linking.
 *
 * P5: Links Core Identity sessions with upstream IdP sessions for audit and single logout.
 * Does not store upstream tokens.
 */
public interface FederationSessionService {

    /**
     * Create a federated session record linking a Core session to the upstream IdP session.
     */
    void createFederatedSession(String sessionId, String connectionId, String externalIdentityId,
                                String upstreamSessionId, String upstreamSubject,
                                long upstreamAuthTime, String upstreamAcr, String upstreamAmrJson, long now);

    /**
     * Revoke Core sessions associated with an external identity (called during SCIM deprovisioning).
     */
    void revokeByExternalIdentity(String externalIdentityId, long now);
}
