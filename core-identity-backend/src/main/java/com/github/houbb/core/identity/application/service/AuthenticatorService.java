package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.Authenticator;

import java.util.List;

/**
 * Authenticator service — lifecycle management for all authenticator types.
 */
public interface AuthenticatorService {

    /**
     * Create a pending authenticator for enrollment.
     */
    Authenticator createPending(String userId, String authenticatorType, String name,
                                String assuranceLevel, int phishingResistant, int userVerificationCapable);

    /**
     * Activate a pending authenticator (marks as ACTIVE and records enrolled_at).
     */
    void activate(String authenticatorId);

    /**
     * Suspend an authenticator temporarily.
     */
    void suspend(String authenticatorId);

    /**
     * Mark an authenticator as compromised.
     */
    void markCompromised(String authenticatorId);

    /**
     * Revoke an authenticator permanently.
     */
    void revoke(String authenticatorId);

    /**
     * List all authenticators for a user.
     */
    List<Authenticator> listByUser(String userId);

    /**
     * Get the highest assurance level among active authenticators for a user.
     */
    String getEffectiveAuthLevel(String userId);

    /**
     * Check if a user has any active authenticator of the given type.
     */
    boolean hasActiveAuthenticator(String userId, String authenticatorType);

    /**
     * Count active authenticators of a given type for a user.
     */
    int countActiveByType(String userId, String authenticatorType);

    /**
     * Rename an authenticator.
     */
    void rename(String authenticatorId, String newName);

    /**
     * Update last used timestamp.
     */
    void recordUsage(String authenticatorId);
}
