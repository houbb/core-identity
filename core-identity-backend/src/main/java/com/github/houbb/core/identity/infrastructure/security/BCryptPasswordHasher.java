package com.github.houbb.core.identity.infrastructure.security;

import com.github.houbb.core.identity.application.port.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * BCrypt implementation of PasswordHasher.
 * Uses BCrypt with strength 12 (default).
 */
public class BCryptPasswordHasher implements PasswordHasher {

    private final PasswordEncoder encoder;

    public BCryptPasswordHasher() {
        this.encoder = new BCryptPasswordEncoder(12);
    }

    public BCryptPasswordHasher(int strength) {
        this.encoder = new BCryptPasswordEncoder(strength);
    }

    @Override
    public String hash(char[] password) {
        String plaintext = new String(password);
        return encoder.encode(plaintext);
    }

    @Override
    public boolean matches(char[] password, String encoded) {
        String plaintext = new String(password);
        return encoder.matches(plaintext, encoded);
    }

    @Override
    public boolean needsRehash(String encoded) {
        BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();
        return bcryptEncoder.upgradeEncoding(encoded);
    }
}