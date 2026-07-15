package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.WebAuthnCredential;

import java.util.Optional;

/**
 * Repository for identity_webauthn_credential.
 */
public interface WebAuthnCredentialRepository {

    void save(WebAuthnCredential credential);

    Optional<WebAuthnCredential> findByCredentialId(String credentialId);

    Optional<WebAuthnCredential> findByAuthenticatorId(String authenticatorId);

    void updateSignCount(String authenticatorId, long signCount, long lastUsedAt);
}
