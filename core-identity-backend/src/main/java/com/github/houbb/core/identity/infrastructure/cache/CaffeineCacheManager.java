package com.github.houbb.core.identity.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Simple caffeine-based in-process cache for authorization snapshots.
 * TTL 5 minutes, max 10000 entries.
 */
public class CaffeineCacheManager {

    private static final Logger log = LoggerFactory.getLogger(CaffeineCacheManager.class);

    private final Cache<String, CachedPermissions> cache;

    public CaffeineCacheManager() {
        this.cache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
    }

    /**
     * Look up cached permissions for a user+org key.
     */
    public CachedPermissions get(String userId, String organizationId) {
        String key = key(userId, organizationId);
        CachedPermissions entry = cache.getIfPresent(key);
        if (entry != null) {
            log.debug("Cache hit for {}", key);
        }
        return entry;
    }

    /**
     * Store permissions for a user+org key.
     */
    public void put(String userId, String organizationId, Set<String> roleIds,
                    Set<String> permissionCodes, long authorizationVersion) {
        String key = key(userId, organizationId);
        CachedPermissions entry = new CachedPermissions(roleIds, permissionCodes, authorizationVersion,
                System.currentTimeMillis());
        cache.put(key, entry);
        log.debug("Cache put for {}", key);
    }

    /**
     * Invalidate cached permissions for a specific user+org.
     */
    public void invalidate(String userId, String organizationId) {
        String key = key(userId, organizationId);
        cache.invalidate(key);
        log.debug("Cache invalidated for {}", key);
    }

    /**
     * Invalidate all cached entries for a given organization.
     */
    public void invalidateOrganization(String organizationId) {
        // Simple approach: we can't efficiently invalidate by partial key,
        // so we invalidate all entries. This is acceptable for P2 scale.
        cache.invalidateAll();
        log.debug("Cache invalidated for organization {}", organizationId);
    }

    private static String key(String userId, String organizationId) {
        return userId + ":" + organizationId;
    }

    /**
     * Cached authorization snapshot for a user in an organization.
     */
    public record CachedPermissions(
            Set<String> roleIds,
            Set<String> permissionCodes,
            long authorizationVersion,
            long cachedAt
    ) {
    }
}
