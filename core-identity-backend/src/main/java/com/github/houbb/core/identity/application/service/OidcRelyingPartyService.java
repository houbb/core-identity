package com.github.houbb.core.identity.application.service;

/**
 * OIDC Relying Party Service — acts as an OpenID Connect Relying Party toward enterprise IdPs.
 *
 * P5: Implements OIDC Authorization Code flow with PKCE. Uses RestTemplate for HTTP calls
 * and JJWT for ID Token validation. No Spring Security OAuth2 client dependencies.
 */
public interface OidcRelyingPartyService {

    record OidcAuthResult(String authorizationUrl, String state, String nonce) {}

    record OidcCallbackResult(String externalSubject, String email, boolean emailVerified,
                              String displayName, String employeeId, java.util.List<String> groups,
                              String acr, String amr, long authTime) {}

    /**
     * Build the authorization URL with state, nonce, and PKCE challenge.
     */
    OidcAuthResult buildAuthorizationRequest(String connectionId);

    /**
     * Handle the OIDC callback: validate state, exchange code for token, validate ID Token,
     * and return verified claims.
     */
    OidcCallbackResult handleCallback(String connectionId, String code, String state, String redirectUri);
}
