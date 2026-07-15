package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.SigningKey;
import com.github.houbb.core.identity.application.port.SigningKeyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Manages signing key lifecycle: creation, rotation, activation, retirement.
 */
public class SigningKeyManager {

    private static final Logger log = LoggerFactory.getLogger(SigningKeyManager.class);

    private static final String ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;
    private static final String SIGNING_ALGORITHM = "RS256";

    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private final SigningKeyRepository repository;
    private final byte[] masterKey;

    public SigningKeyManager(SigningKeyRepository repository, String masterKeySecret) {
        this.repository = repository;
        this.masterKey = masterKeySecret.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Create a new key pair and save to DB (private key encrypted with master key).
     */
    public SigningKey createKey() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance(ALGORITHM);
            gen.initialize(KEY_SIZE, new SecureRandom());
            KeyPair pair = gen.generateKeyPair();

            String publicKeyPem = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
            String encryptedPrivateKey = encryptPrivateKey(pair.getPrivate().getEncoded());

            long now = System.currentTimeMillis();
            String kid = "sig-" + UUID.randomUUID().toString().substring(0, 8);
            SigningKey key = new SigningKey();
            key.setId(UUID.randomUUID().toString());
            key.setKeyId(kid);
            key.setAlgorithm(SIGNING_ALGORITHM);
            key.setPublicKey(publicKeyPem);
            key.setEncryptedPrivateKey(encryptedPrivateKey);
            key.setStatus("PENDING");
            key.setActiveFrom(now);
            key.setCreatedAt(now);
            key.setUpdatedAt(now);
            repository.save(key);
            log.info("Created signing key: {}", kid);
            return key;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create signing key", e);
        }
    }

    /**
     * Activate a key (set to ACTIVE).
     */
    public void activateKey(String keyId) {
        SigningKey key = repository.findByKeyId(keyId)
                .orElseThrow(() -> new IllegalArgumentException("Signing key not found: " + keyId));
        long now = System.currentTimeMillis();

        // Retire existing active key
        List<SigningKey> activeKeys = repository.findByStatus("ACTIVE");
        for (SigningKey active : activeKeys) {
            if (!active.getKeyId().equals(keyId)) {
                active.setStatus("RETIRING");
                active.setRetireAfter(now + 30 * 60 * 1000); // 30 min overlap
                active.setUpdatedAt(now);
                repository.update(active);
                log.info("Retiring signing key: {}", active.getKeyId());
            }
        }

        key.setStatus("ACTIVE");
        key.setActiveFrom(now);
        key.setUpdatedAt(now);
        repository.update(key);
        log.info("Activated signing key: {}", keyId);
    }

    /**
     * Retire a key.
     */
    public void retireKey(String keyId) {
        SigningKey key = repository.findByKeyId(keyId)
                .orElseThrow(() -> new IllegalArgumentException("Signing key not found: " + keyId));
        long now = System.currentTimeMillis();
        key.setStatus("RETIRED");
        key.setUpdatedAt(now);
        repository.update(key);
        log.info("Retired signing key: {}", keyId);
    }

    /**
     * Revoke a key immediately (compromised key).
     */
    public void revokeKey(String keyId) {
        SigningKey key = repository.findByKeyId(keyId)
                .orElseThrow(() -> new IllegalArgumentException("Signing key not found: " + keyId));
        long now = System.currentTimeMillis();
        key.setStatus("REVOKED");
        key.setUpdatedAt(now);
        repository.update(key);
        log.warn("Revoked signing key: {}", keyId);
    }

    /**
     * Get the currently active private key for signing.
     */
    public PrivateKey getActivePrivateKey() {
        List<SigningKey> activeKeys = repository.findByStatus("ACTIVE");
        if (activeKeys.isEmpty()) {
            throw new IllegalStateException("No active signing key found");
        }
        SigningKey key = activeKeys.get(0);
        return decryptPrivateKey(key.getEncryptedPrivateKey());
    }

    /**
     * Get active key ID (kid for JWT header).
     */
    public String getActiveKid() {
        List<SigningKey> activeKeys = repository.findByStatus("ACTIVE");
        if (activeKeys.isEmpty()) {
            throw new IllegalStateException("No active signing key found");
        }
        return activeKeys.get(0).getKeyId();
    }

    /**
     * Get public key by kid for JWT verification.
     */
    public PublicKey getPublicKeyByKid(String kid) {
        SigningKey key = repository.findByKeyId(kid).orElse(null);
        if (key == null) return null;
        try {
            byte[] keyBytes = Base64.getDecoder().decode(key.getPublicKey());
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance(ALGORITHM);
            return kf.generatePublic(spec);
        } catch (Exception e) {
            log.error("Failed to parse public key for kid: {}", kid, e);
            return null;
        }
    }

    /**
     * Get all active/retiring keys for JWKS.
     */
    public List<SigningKey> getJwksKeys() {
        return repository.findAllActive();
    }

    // === Encryption helpers ===

    private String encryptPrivateKey(byte[] privateKeyBytes) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        SecretKeySpec aesKey = deriveAesKey();
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, spec);

        byte[] encrypted = cipher.doFinal(privateKeyBytes);
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    private PrivateKey decryptPrivateKey(String encryptedBase64) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedBase64);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(combined, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

            SecretKeySpec aesKey = deriveAesKey();
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, aesKey, spec);

            byte[] privateKeyBytes = cipher.doFinal(encrypted);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory kf = KeyFactory.getInstance(ALGORITHM);
            return kf.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt private key", e);
        }
    }

    private SecretKeySpec deriveAesKey() throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = sha.digest(masterKey);
        return new SecretKeySpec(key, "AES");
    }
}