package com.github.houbb.core.identity.infrastructure.configuration;

import com.github.houbb.core.identity.application.domain.SigningKey;
import com.github.houbb.core.identity.application.service.SigningKeyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Bootstrap runner that ensures at least one ACTIVE signing key exists on startup.
 * If none, creates and activates one automatically.
 */
@Component
public class SigningKeyBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SigningKeyBootstrapRunner.class);

    private final SigningKeyManager keyManager;

    public SigningKeyBootstrapRunner(SigningKeyManager keyManager) {
        this.keyManager = keyManager;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            var activeKeys = keyManager.getJwksKeys();
            if (activeKeys.isEmpty()) {
                log.info("No signing key found, generating bootstrap key...");
                SigningKey newKey = keyManager.createKey();
                keyManager.activateKey(newKey.getKeyId());
                log.info("Bootstrap signing key created and activated: {}", newKey.getKeyId());
            }
        } catch (Exception e) {
            log.error("Failed to ensure signing key availability: {}", e.getMessage(), e);
        }
    }
}