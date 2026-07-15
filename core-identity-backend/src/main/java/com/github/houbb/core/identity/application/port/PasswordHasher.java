package com.github.houbb.core.identity.application.port;

/**
 * Password hashing port.
 * Default implementation uses BCrypt, future versions can swap algorithm.
 */
public interface PasswordHasher {

    /**
     * Hash a plaintext password.
     */
    String hash(char[] password);

    /**
     * Verify a plaintext password against an encoded hash.
     */
    boolean matches(char[] password, String encoded);

    /**
     * Check if an encoded hash needs rehashing (e.g., algorithm upgrade).
     */
    boolean needsRehash(String encoded);
}