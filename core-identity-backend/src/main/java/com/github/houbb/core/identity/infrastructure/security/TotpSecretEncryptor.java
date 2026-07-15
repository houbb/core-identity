package com.github.houbb.core.identity.infrastructure.security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM encryptor for TOTP secrets.
 * Uses a separate key (core.totp.encryption-key) — not the oauth master key.
 */
public class TotpSecretEncryptor {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final String KEY_VERSION = "v1";

    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public TotpSecretEncryptor(String encryptionKey) {
        // Ensure key is exactly 32 bytes for AES-256
        byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
        byte[] key32 = new byte[32];
        System.arraycopy(keyBytes, 0, key32, 0, Math.min(keyBytes.length, 32));
        this.secretKey = new SecretKeySpec(key32, "AES");
    }

    /**
     * Encrypt a TOTP secret. Returns base64(IV || ciphertext).
     */
    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            buffer.put(iv);
            buffer.put(ciphertext);

            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt TOTP secret", e);
        }
    }

    /**
     * Decrypt a TOTP secret from base64(IV || ciphertext).
     */
    public String decrypt(String encrypted) {
        try {
            byte[] data = Base64.getDecoder().decode(encrypted);
            ByteBuffer buffer = ByteBuffer.wrap(data);

            byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);

            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt TOTP secret", e);
        }
    }

    /**
     * Current key version for rotation tracking.
     */
    public String getKeyVersion() {
        return KEY_VERSION;
    }
}
