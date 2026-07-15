package com.github.houbb.core.identity.application.service;

/**
 * TOTP service — enrollment, confirmation, and verification.
 */
public interface TotpService {

    /**
     * Start TOTP enrollment. Creates PENDING authenticator and returns enrollment data.
     */
    TotpEnrollmentResult enroll(String userId, String issuer, String accountName);

    /**
     * Confirm TOTP enrollment with a verification code.
     */
    void confirm(String userId, String authenticatorId, String code);

    /**
     * Verify a TOTP code during login or step-up authentication.
     */
    boolean verify(String userId, String code);

    /**
     * Cancel a pending TOTP enrollment.
     */
    void cancelEnrollment(String authenticatorId);

    record TotpEnrollmentResult(String authenticatorId, String qrCodeUri, String manualKey) {}
}
