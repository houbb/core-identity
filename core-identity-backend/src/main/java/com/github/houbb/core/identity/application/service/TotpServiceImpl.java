package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.Authenticator;
import com.github.houbb.core.identity.application.domain.TotpAuthenticator;
import com.github.houbb.core.identity.application.port.AuthenticatorRepository;
import com.github.houbb.core.identity.application.port.TotpAuthenticatorRepository;
import com.github.houbb.core.identity.infrastructure.security.TotpSecretEncryptor;
import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Base32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

/**
 * Default implementation of TotpService using aerogear-otp-java.
 */
public class TotpServiceImpl implements TotpService {

    private static final Logger log = LoggerFactory.getLogger(TotpServiceImpl.class);

    private static final String ALGORITHM = "SHA1";
    private static final int DIGITS = 6;
    private static final int PERIOD_SECONDS = 30;

    private final TotpAuthenticatorRepository totpRepo;
    private final AuthenticatorRepository authenticatorRepo;
    private final AuthenticatorService authenticatorService;
    private final RecoveryCodeService recoveryCodeService;
    private final TotpSecretEncryptor encryptor;
    private final SecureRandom secureRandom = new SecureRandom();

    public TotpServiceImpl(TotpAuthenticatorRepository totpRepo,
                          AuthenticatorRepository authenticatorRepo,
                          AuthenticatorService authenticatorService,
                          RecoveryCodeService recoveryCodeService,
                          TotpSecretEncryptor encryptor) {
        this.totpRepo = totpRepo;
        this.authenticatorRepo = authenticatorRepo;
        this.authenticatorService = authenticatorService;
        this.recoveryCodeService = recoveryCodeService;
        this.encryptor = encryptor;
    }

    @Override
    @Transactional
    public TotpEnrollmentResult enroll(String userId, String issuer, String accountName) {
        // Create PENDING authenticator
        Authenticator auth = authenticatorService.createPending(
                userId, "TOTP", "Authenticator App",
                "AUTH_LEVEL_2", 0, 0);

        // Generate TOTP secret
        byte[] secretBytes = new byte[20]; // 160 bits for SHA1
        secureRandom.nextBytes(secretBytes);
        String base32Secret = Base32.encode(secretBytes);

        // Encrypt and store
        String encryptedSecret = encryptor.encrypt(base32Secret);
        long now = System.currentTimeMillis();
        TotpAuthenticator totp = new TotpAuthenticator();
        totp.setAuthenticatorId(auth.getId());
        totp.setEncryptedSecret(encryptedSecret);
        totp.setEncryptionKeyVersion(encryptor.getKeyVersion());
        totp.setAlgorithm(ALGORITHM);
        totp.setDigits(DIGITS);
        totp.setPeriodSeconds(PERIOD_SECONDS);
        totp.setLastAcceptedStep(0);
        totpRepo.save(totp);

        // Generate QR code URI
        String label = issuer + ":" + accountName;
        String qrCodeUri = String.format("otpauth://totp/%s?secret=%s&issuer=%s&algorithm=%s&digits=%d&period=%d",
                label, base32Secret, issuer, ALGORITHM, DIGITS, PERIOD_SECONDS);

        log.info("TOTP enrollment started for user {}: {}", userId, auth.getId());
        return new TotpEnrollmentResult(auth.getId(), qrCodeUri, base32Secret);
    }

    @Override
    @Transactional
    public void confirm(String userId, String authenticatorId, String code) {
        Authenticator auth = authenticatorRepo.findById(authenticatorId)
                .orElseThrow(() -> new AuthenticatorServiceImpl.AuthenticatorException(
                        "IDENTITY_AUTHENTICATOR_NOT_FOUND", "Authenticator not found"));

        if (!auth.getUserId().equals(userId)) {
            throw new AuthenticatorServiceImpl.AuthenticatorException(
                    "IDENTITY_AUTHENTICATOR_NOT_FOUND", "Authenticator does not belong to user");
        }

        if (!"PENDING".equals(auth.getStatus())) {
            throw new AuthenticatorServiceImpl.AuthenticatorException(
                    "IDENTITY_AUTHENTICATOR_NOT_ACTIVE", "Authenticator is not in PENDING status");
        }

        TotpAuthenticator totp = totpRepo.findByAuthenticatorId(authenticatorId)
                .orElseThrow(() -> new AuthenticatorServiceImpl.AuthenticatorException(
                        "IDENTITY_AUTHENTICATOR_NOT_FOUND", "TOTP authenticator not found"));

        String secret = encryptor.decrypt(totp.getEncryptedSecret());
        Totp totpValidator = new Totp(secret);

        if (!totpValidator.verify(code)) {
            throw new TotpException("IDENTITY_TOTP_INVALID", "Invalid TOTP verification code");
        }

        // Activate the authenticator
        authenticatorService.activate(authenticatorId);

        // Record confirmation
        long now = System.currentTimeMillis();
        totpRepo.updateLastAcceptedStep(authenticatorId, now / (PERIOD_SECONDS * 1000L), now);

        // Generate recovery codes if this is the first MFA
        if (!authenticatorService.hasActiveAuthenticator(userId, "TOTP") &&
            authenticatorService.countActiveByType(userId, "TOTP") <= 1) {
            recoveryCodeService.generate(userId);
        }

        log.info("TOTP enrollment confirmed for user {}: {}", userId, authenticatorId);
    }

    @Override
    public boolean verify(String userId, String code) {
        // Find the first active TOTP authenticator for this user
        var activeTotp = authenticatorRepo.findByUserIdAndStatus(userId, "ACTIVE").stream()
                .filter(a -> "TOTP".equals(a.getAuthenticatorType()))
                .findFirst();

        if (activeTotp.isEmpty()) {
            log.warn("No active TOTP authenticator found for user {}", userId);
            return false;
        }

        Authenticator auth = activeTotp.get();
        TotpAuthenticator totp = totpRepo.findByAuthenticatorId(auth.getId())
                .orElse(null);

        if (totp == null) {
            log.warn("No TOTP record found for authenticator {}", auth.getId());
            return false;
        }

        String secret = encryptor.decrypt(totp.getEncryptedSecret());
        Totp totpValidator = new Totp(secret);

        if (!totpValidator.verify(code)) {
            return false;
        }

        // Replay protection: check last accepted step
        long currentStep = System.currentTimeMillis() / (PERIOD_SECONDS * 1000L);
        if (currentStep <= totp.getLastAcceptedStep()) {
            log.warn("TOTP replay detected for authenticator {}", auth.getId());
            throw new TotpException("IDENTITY_TOTP_REPLAYED", "TOTP code has already been used");
        }

        // Update last accepted step
        authenticatorService.recordUsage(auth.getId());
        totpRepo.updateLastAcceptedStep(auth.getId(), currentStep, System.currentTimeMillis());
        return true;
    }

    @Override
    @Transactional
    public void cancelEnrollment(String authenticatorId) {
        Authenticator auth = authenticatorRepo.findById(authenticatorId)
                .orElseThrow(() -> new AuthenticatorServiceImpl.AuthenticatorException(
                        "IDENTITY_AUTHENTICATOR_NOT_FOUND", "Authenticator not found"));

        if (!"PENDING".equals(auth.getStatus())) {
            throw new AuthenticatorServiceImpl.AuthenticatorException(
                    "IDENTITY_AUTHENTICATOR_NOT_ACTIVE", "Authenticator is not in PENDING status");
        }

        // Delete TOTP record
        totpRepo.deleteByAuthenticatorId(authenticatorId);

        // Revoke the authenticator
        authenticatorService.revoke(authenticatorId);
        log.info("TOTP enrollment cancelled: {}", authenticatorId);
    }

    public static class TotpException extends RuntimeException {
        private final String errorCode;

        public TotpException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }

        public String getErrorCode() { return errorCode; }
    }
}
