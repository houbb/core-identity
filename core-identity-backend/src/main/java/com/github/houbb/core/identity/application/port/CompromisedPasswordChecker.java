package com.github.houbb.core.identity.application.port;

/**
 * Checks whether a password has been exposed in known data breaches.
 */
public interface CompromisedPasswordChecker {

    /**
     * Check a password against known compromised password lists.
     * The password char[] is zeroed after use.
     *
     * @return result indicating whether the password is compromised
     */
    PasswordExposureResult check(char[] password);

    record PasswordExposureResult(boolean isCompromised, int exposureCount, String source) {
        public static PasswordExposureResult safe() {
            return new PasswordExposureResult(false, 0, "none");
        }
        public static PasswordExposureResult compromised(int count, String source) {
            return new PasswordExposureResult(true, count, source);
        }
    }
}
