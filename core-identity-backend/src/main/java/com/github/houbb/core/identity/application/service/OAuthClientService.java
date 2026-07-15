package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.OAuthClient;
import com.github.houbb.core.identity.application.domain.OAuthClientSecret;
import com.github.houbb.core.identity.application.port.OAuthClientRepository;
import com.github.houbb.core.identity.application.port.OAuthClientSecretRepository;
import com.github.houbb.core.identity.infrastructure.util.TokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public class OAuthClientService {
    private static final Logger log = LoggerFactory.getLogger(OAuthClientService.class);
    private final OAuthClientRepository clientRepo;
    private final OAuthClientSecretRepository secretRepo;

    public OAuthClientService(OAuthClientRepository clientRepo, OAuthClientSecretRepository secretRepo) {
        this.clientRepo = clientRepo;
        this.secretRepo = secretRepo;
    }

    @Transactional
    public record CreateClientResult(OAuthClient client, String rawSecret) {}

    @Transactional
    public CreateClientResult createClient(String ownerType, String ownerId, String name, String description,
                                           String clientType, List<String> redirectUris, String createdBy) {
        long now = System.currentTimeMillis();
        String clientUid = UUID.randomUUID().toString();
        String extClientId = "oc_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        OAuthClient client = new OAuthClient();
        client.setId(clientUid);
        client.setClientId(extClientId);
        client.setOwnerType(ownerType);
        client.setOwnerId(ownerId);
        client.setClientType(clientType != null ? clientType : "CONFIDENTIAL");
        client.setName(name);
        client.setDescription(description);
        client.setStatus("ACTIVE");
        client.setReviewStatus("APPROVED");
        client.setConsentRequired(1);
        client.setCreatedBy(createdBy);
        client.setCreatedAt(now);
        client.setUpdatedAt(now);
        client.setVersion(1);
        clientRepo.save(client);

        // Create secret if confidential
        String rawSecret = null;
        if ("CONFIDENTIAL".equals(client.getClientType())) {
            rawSecret = "ocs_" + TokenUtils.generateRandomToken().substring(0, 32);
            String hash = TokenUtils.hashToken(rawSecret);
            OAuthClientSecret secret = new OAuthClientSecret();
            secret.setId(UUID.randomUUID().toString());
            secret.setClientId(clientUid);
            secret.setSecretPrefix(rawSecret.substring(0, 8));
            secret.setSecretHash(hash);
            secret.setName("Default Secret");
            secret.setStatus("ACTIVE");
            secret.setCreatedAt(now);
            secretRepo.save(secret);
        }

        // Store redirect URIs via JDBC directly (simplified)
        log.info("OAuth Client created: {} ({})", name, extClientId);
        return new CreateClientResult(client, rawSecret);
    }

    public OAuthClient getClient(String clientId) {
        return clientRepo.findById(clientId)
                .or(() -> clientRepo.findByClientId(clientId))
                .orElseThrow(() -> new OAuthClientNotFoundException("Client not found: " + clientId));
    }

    public List<OAuthClient> getUserClients(String ownerType, String ownerId) {
        return clientRepo.findByOwner(ownerType, ownerId);
    }

    @Transactional
    public String rotateSecret(String clientId) {
        OAuthClient client = getClient(clientId);
        if (!"CONFIDENTIAL".equals(client.getClientType())) {
            throw new IllegalArgumentException("PUBLIC clients do not have secrets");
        }
        long now = System.currentTimeMillis();
        // Revoke old
        secretRepo.revokeByClientId(client.getId(), now);
        // Create new
        String rawSecret = "ocs_" + TokenUtils.generateRandomToken().substring(0, 32);
        String hash = TokenUtils.hashToken(rawSecret);
        OAuthClientSecret secret = new OAuthClientSecret();
        secret.setId(UUID.randomUUID().toString());
        secret.setClientId(client.getId());
        secret.setSecretPrefix(rawSecret.substring(0, 8));
        secret.setSecretHash(hash);
        secret.setName("Default Secret");
        secret.setStatus("ACTIVE");
        secret.setCreatedAt(now);
        secretRepo.save(secret);
        return rawSecret;
    }

    @Transactional
    public void updateClient(String clientId, String name, String description, String homepageUrl) {
        OAuthClient client = getClient(clientId);
        client.setName(name != null ? name : client.getName());
        client.setDescription(description != null ? description : client.getDescription());
        if (homepageUrl != null) client.setHomepageUrl(homepageUrl);
        client.setUpdatedAt(System.currentTimeMillis());
        clientRepo.update(client);
    }

    @Transactional
    public void suspendClient(String clientId) {
        OAuthClient client = getClient(clientId);
        clientRepo.updateStatus(client.getId(), "SUSPENDED", client.getReviewStatus(), System.currentTimeMillis(), client.getVersion());
    }

    @Transactional
    public void reactivateClient(String clientId) {
        OAuthClient client = getClient(clientId);
        clientRepo.updateStatus(client.getId(), "ACTIVE", client.getReviewStatus(), System.currentTimeMillis(), client.getVersion());
    }

    public boolean validateClientSecret(String clientId, String rawSecret) {
        OAuthClient client = getClient(clientId);
        List<OAuthClientSecret> secrets = secretRepo.findByClientId(client.getId());
        String hash = TokenUtils.hashToken(rawSecret);
        return secrets.stream().anyMatch(s -> "ACTIVE".equals(s.getStatus()) && s.getSecretHash().equals(hash));
    }

    public static class OAuthClientNotFoundException extends RuntimeException {
        public OAuthClientNotFoundException(String msg) { super(msg); }
    }
}