package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.Authenticator;
import com.github.houbb.core.identity.application.port.AuthenticatorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

/**
 * Default implementation of AuthenticatorService.
 */
public class AuthenticatorServiceImpl implements AuthenticatorService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticatorServiceImpl.class);

    private static final String[] AUTH_LEVEL_ORDER = {"AUTH_LEVEL_1", "AUTH_LEVEL_2", "AUTH_LEVEL_3"};

    private final AuthenticatorRepository authenticatorRepo;

    public AuthenticatorServiceImpl(AuthenticatorRepository authenticatorRepo) {
        this.authenticatorRepo = authenticatorRepo;
    }

    @Override
    public Authenticator createPending(String userId, String authenticatorType, String name,
                                       String assuranceLevel, int phishingResistant, int userVerificationCapable) {
        long now = System.currentTimeMillis();
        Authenticator a = new Authenticator();
        a.setId(UUID.randomUUID().toString());
        a.setUserId(userId);
        a.setAuthenticatorType(authenticatorType);
        a.setName(name);
        a.setStatus("PENDING");
        a.setAssuranceLevel(assuranceLevel);
        a.setPhishingResistant(phishingResistant);
        a.setUserVerificationCapable(userVerificationCapable);
        a.setCreatedAt(now);
        a.setUpdatedAt(now);
        a.setVersion(1);
        authenticatorRepo.save(a);
        log.info("Created PENDING authenticator {} ({}) for user {}", a.getId(), authenticatorType, userId);
        return a;
    }

    @Override
    public void activate(String authenticatorId) {
        Authenticator a = authenticatorRepo.findById(authenticatorId)
                .orElseThrow(() -> new AuthenticatorException("IDENTITY_AUTHENTICATOR_NOT_FOUND", "Authenticator not found"));
        if (!"PENDING".equals(a.getStatus())) {
            throw new AuthenticatorException("IDENTITY_AUTHENTICATOR_NOT_ACTIVE", "Authenticator is not in PENDING status");
        }
        long now = System.currentTimeMillis();
        a.setStatus("ACTIVE");
        a.setEnrolledAt(now);
        a.setUpdatedAt(now);
        authenticatorRepo.update(a);
        log.info("Activated authenticator {}", authenticatorId);
    }

    @Override
    public void suspend(String authenticatorId) {
        Authenticator a = authenticatorRepo.findById(authenticatorId)
                .orElseThrow(() -> new AuthenticatorException("IDENTITY_AUTHENTICATOR_NOT_FOUND", "Authenticator not found"));
        long now = System.currentTimeMillis();
        a.setStatus("SUSPENDED");
        a.setUpdatedAt(now);
        authenticatorRepo.update(a);
        log.info("Suspended authenticator {}", authenticatorId);
    }

    @Override
    public void markCompromised(String authenticatorId) {
        Authenticator a = authenticatorRepo.findById(authenticatorId)
                .orElseThrow(() -> new AuthenticatorException("IDENTITY_AUTHENTICATOR_NOT_FOUND", "Authenticator not found"));
        long now = System.currentTimeMillis();
        a.setStatus("COMPROMISED");
        a.setCompromisedAt(now);
        a.setUpdatedAt(now);
        authenticatorRepo.update(a);
        log.info("Marked authenticator {} as compromised", authenticatorId);
    }

    @Override
    public void revoke(String authenticatorId) {
        Authenticator a = authenticatorRepo.findById(authenticatorId)
                .orElseThrow(() -> new AuthenticatorException("IDENTITY_AUTHENTICATOR_NOT_FOUND", "Authenticator not found"));
        long now = System.currentTimeMillis();
        a.setStatus("REVOKED");
        a.setRevokedAt(now);
        a.setUpdatedAt(now);
        authenticatorRepo.update(a);
        log.info("Revoked authenticator {}", authenticatorId);
    }

    @Override
    public List<Authenticator> listByUser(String userId) {
        return authenticatorRepo.findByUserId(userId);
    }

    @Override
    public String getEffectiveAuthLevel(String userId) {
        List<Authenticator> active = authenticatorRepo.findByUserIdAndStatus(userId, "ACTIVE");
        int maxLevel = 0;
        for (Authenticator a : active) {
            String level = a.getAssuranceLevel();
            for (int i = 0; i < AUTH_LEVEL_ORDER.length; i++) {
                if (AUTH_LEVEL_ORDER[i].equals(level) && i > maxLevel) {
                    maxLevel = i;
                }
            }
        }
        return AUTH_LEVEL_ORDER[maxLevel];
    }

    @Override
    public boolean hasActiveAuthenticator(String userId, String authenticatorType) {
        return authenticatorRepo.countActiveByUserIdAndType(userId, authenticatorType) > 0;
    }

    @Override
    public int countActiveByType(String userId, String authenticatorType) {
        return authenticatorRepo.countActiveByUserIdAndType(userId, authenticatorType);
    }

    @Override
    public void rename(String authenticatorId, String newName) {
        Authenticator a = authenticatorRepo.findById(authenticatorId)
                .orElseThrow(() -> new AuthenticatorException("IDENTITY_AUTHENTICATOR_NOT_FOUND", "Authenticator not found"));
        a.setName(newName);
        a.setUpdatedAt(System.currentTimeMillis());
        authenticatorRepo.update(a);
    }

    @Override
    public void recordUsage(String authenticatorId) {
        authenticatorRepo.updateLastUsedAt(authenticatorId, System.currentTimeMillis(), 0);
    }

    public static class AuthenticatorException extends RuntimeException {
        private final String errorCode;

        public AuthenticatorException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }

        public String getErrorCode() { return errorCode; }
    }
}