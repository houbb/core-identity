package com.github.houbb.core.identity.application.service;

import java.util.Map;

/**
 * WebAuthn/Passkey service — registration and authentication.
 */
public interface WebAuthnService {

    /**
     * Generate PublicKeyCredentialCreationOptions for registration.
     */
    WebAuthnRegistrationOptions generateRegistrationOptions(String userId, String userName);

    /**
     * Verify the registration response and create a WebAuthn credential.
     */
    void verifyRegistration(String userId, String authenticatorId, Map<String, Object> response);

    /**
     * Generate PublicKeyCredentialRequestOptions for authentication.
     * If email is provided, includes allowCredentials for the user.
     */
    WebAuthnAuthenticationOptions generateAuthenticationOptions(String email);

    /**
     * Verify the authentication response. Returns the authenticated userId.
     */
    String verifyAuthentication(Map<String, Object> response);

    record WebAuthnRegistrationOptions(String challenge, String rpId, String rpName, String userId,
                                       String userName, String userDisplayName, long timeout) {}

    record WebAuthnAuthenticationOptions(String challenge, String rpId, String allowCredentialsJson, long timeout) {}
}
