package com.github.houbb.core.identity.application.service;

import com.github.houbb.core.identity.application.domain.*;
import com.github.houbb.core.identity.application.port.*;
import com.github.houbb.core.identity.infrastructure.util.TokenUtils;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

public class ApiKeyService {
    private final ApiKeyRepository keyRepo;
    private final OAuthTokenService tokenService;
    private final int accessTokenTtl;

    public ApiKeyService(ApiKeyRepository keyRepo, OAuthTokenService tokenService, int accessTokenTtl) {
        this.keyRepo=keyRepo; this.tokenService=tokenService; this.accessTokenTtl=accessTokenTtl;
    }

    public record KeyResult(ApiKey key, String rawKey) {}

    @Transactional
    public KeyResult createKey(String name, String ownerType, String ownerId, String organizationId,
                                List<String> scopeIds, List<String> audienceIds, Long expiresAt) {
        long now = System.currentTimeMillis();
        String rawKey = "ak_" + TokenUtils.generateRandomToken().substring(0, 40);
        String hash = TokenUtils.hashToken(rawKey);
        String prefix = rawKey.substring(0, 8);

        ApiKey k = new ApiKey();
        k.setId(UUID.randomUUID().toString());
        k.setKeyPrefix(prefix);
        k.setKeyHash(hash);
        k.setName(name);
        k.setOwnerType(ownerType);
        k.setOwnerId(ownerId);
        k.setOrganizationId(organizationId);
        k.setStatus("ACTIVE");
        k.setExpiresAt(expiresAt);
        k.setCreatedAt(now);
        k.setVersion(1);
        keyRepo.save(k);
        return new KeyResult(k, rawKey);
    }

    @Transactional
    public void revokeKey(String keyId) { keyRepo.revoke(keyId, System.currentTimeMillis()); }
    public List<ApiKey> getUserKeys(String userId) { return keyRepo.findByOwner("USER", userId); }
    public List<ApiKey> getOrgKeys(String orgId) { return keyRepo.findByOrg(orgId); }

    /** Exchange API Key for JWT */
    public String exchangeKeyForToken(String rawKey) {
        String hash = TokenUtils.hashToken(rawKey);
        ApiKey k = keyRepo.findByHash(hash).orElseThrow(() -> new RuntimeException("Invalid API Key"));
        if (!"ACTIVE".equals(k.getStatus())) throw new RuntimeException("API Key is " + k.getStatus());
        if (k.getExpiresAt() != null && k.getExpiresAt() < System.currentTimeMillis()) throw new RuntimeException("API Key expired");
        // Update last used
        k.setLastUsedAt(System.currentTimeMillis());
        keyRepo.update(k);
        // Issue a short-lived token
        return tokenService.issueAccessToken(k.getOwnerId(), "api_key", "core-identity",
                k.getKeyPrefix(), k.getOrganizationId(), "api", accessTokenTtl);
    }

    /**
     * Introspect an API key — validate it and return owner information.
     * Used by Gateway for API Key verification without issuing a full JWT.
     */
    public ApiKeyIntrospection introspect(String rawKey) {
        String hash = TokenUtils.hashToken(rawKey);
        ApiKey k = keyRepo.findByHash(hash).orElse(null);
        if (k == null) {
            return new ApiKeyIntrospection(false, null, null, null, null, null);
        }
        if (!"ACTIVE".equals(k.getStatus())) {
            return new ApiKeyIntrospection(false, null, null, null, null, k.getStatus());
        }
        if (k.getExpiresAt() != null && k.getExpiresAt() < System.currentTimeMillis()) {
            return new ApiKeyIntrospection(false, null, null, null, null, "EXPIRED");
        }
        // Update last used
        k.setLastUsedAt(System.currentTimeMillis());
        keyRepo.update(k);

        return new ApiKeyIntrospection(true, k.getOwnerType(), k.getOwnerId(),
                k.getOrganizationId(), k.getKeyPrefix(), "ACTIVE");
    }

    public record ApiKeyIntrospection(boolean active, String ownerType, String ownerId,
                                       String organizationId, String keyPrefix, String status) {}
}