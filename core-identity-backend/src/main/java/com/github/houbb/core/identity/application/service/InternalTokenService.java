package com.github.houbb.core.identity.application.service;

/**
 * Internal token service for service-to-service authentication.
 */
public interface InternalTokenService {

    /**
     * Issue a short-lived service token.
     */
    String issueToken(String clientId, String clientSecret);

    /**
     * Validate a service token and return the client ID.
     */
    String validateToken(String token);

    /**
     * Check if token has required scopes.
     */
    boolean hasScope(String token, String requiredScope);
}