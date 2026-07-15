package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.TotpAuthenticator;

import java.util.Optional;

/**
 * Repository for identity_totp_authenticator.
 */
public interface TotpAuthenticatorRepository {

    void save(TotpAuthenticator totp);

    Optional<TotpAuthenticator> findByAuthenticatorId(String authenticatorId);

    void updateLastAcceptedStep(String authenticatorId, long lastAcceptedStep, long confirmedAt);

    void deleteByAuthenticatorId(String authenticatorId);
}
