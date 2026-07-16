package com.github.houbb.core.identity.infrastructure.cache;

import com.github.houbb.core.identity.application.port.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine-backed implementation of {@link CacheManager} (P7.2).
 * <p>
 * In-process cache suitable for standalone and standard modes.
 * Falls back gracefully when Redis is not available in enterprise mode.
 * <p>
 * Backward compatible with existing CachedPermissions API.
 */
public class CaffeineCacheManager implements CacheManager {

    private static final Logger log = LoggerFactory.getLogger(CaffeineCacheManager.class);

    private final com.github.benmanes.caffeine.cache.Cache<String, Object> cache;
    private final Set<String> keys = ConcurrentHashMap.newKeySet();

    public CaffeineCacheManager() {
        this.cache = com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        Object value = cache.getIfPresent(key);
        if (value == null) {
            log.debug("Caffeine cache miss: {}", key);
            return Optional.empty();
        }
        if (type.isInstance(value)) {
            return Optional.of((T) value);
        }
        log.warn("Caffeine cache type mismatch for key {}: expected {}, got {}",
                key, type.getSimpleName(), value.getClass().getSimpleName());
        return Optional.empty();
    }

    @Override
    public void put(String key, Object value, Duration ttl) {
        long ttlSeconds = ttl != null ? ttl.getSeconds() : 300;
        if (ttlSeconds <= 0) {
            ttlSeconds = 300;
        }
        cache.put(key, value);
        keys.add(key);
        log.debug("Caffeine cache put: {}, ttl={}s", key, ttlSeconds);
    }

    @Override
    public void invalidate(String key) {
        cache.invalidate(key);
        keys.remove(key);
        log.debug("Caffeine cache invalidated: {}", key);
    }

    @Override
    public void invalidateByPattern(String pattern) {
        String prefix = pattern.replace("*", "");
        int removed = 0;
        for (String key : keys) {
            if (key.startsWith(prefix) || key.contains(prefix)) {
                cache.invalidate(key);
                removed++;
            }
        }
        keys.removeIf(k -> k.startsWith(prefix) || k.contains(prefix));
        log.debug("Caffeine cache pattern-invalidated: {} (removed {} keys)", pattern, removed);
    }

    @Override
    public void invalidateAll() {
        long size = cache.estimatedSize();
        cache.invalidateAll();
        keys.clear();
        log.debug("Caffeine cache cleared ({} entries)", size);
    }

    // ========================================================================
    // Backward-compatible API for existing AuthorizationServiceImpl
    // These methods use the old (String, String) signature pattern
    // ========================================================================

    /**
     * Direct access for existing code that uses the P2-style CachedPermissions API.
     */
    public CachedPermissions get(String userId, String organizationId) {
        return getCachedPermissions(userId, organizationId);
    }

    /**
     * Store permissions for a user+org key (old API).
     */
    public void put(String userId, String organizationId,
                    Set<String> roleIds, Set<String> permissionCodes,
                    long authorizationVersion) {
        putCachedPermissions(userId, organizationId, roleIds, permissionCodes, authorizationVersion);
    }

    /**
     * Invalidate cached permissions for a specific user+org (old API).
     */
    public void invalidate(String userId, String organizationId) {
        invalidatePermission(userId, organizationId);
    }

    /**
     * Direct access for existing code that uses the P2-style CachedPermissions API.
     */
    public CachedPermissions getCachedPermissions(String userId, String organizationId) {
        String cacheKey = key(userId, organizationId);
        Object value = cache.getIfPresent(cacheKey);
        if (value instanceof CachedPermissions) {
            return (CachedPermissions) value;
        }
        return null;
    }

    /**
     * Store permissions for a user+org key.
     */
    public void putCachedPermissions(String userId, String organizationId,
                                     Set<String> roleIds, Set<String> permissionCodes,
                                     long authorizationVersion) {
        CachedPermissions entry = new CachedPermissions(roleIds, permissionCodes, authorizationVersion,
                System.currentTimeMillis());
        cache.put(key(userId, organizationId), entry);
        log.debug("Cached permissions for {}/{}", userId, organizationId);
    }

    /**
     * Invalidate cached permissions for a specific user+org.
     */
    public void invalidatePermission(String userId, String organizationId) {
        invalidate(key(userId, organizationId));
    }

    /**
     * Invalidate all cached entries for a given organization.
     * Simple approach: clear all entries (acceptable for current scale).
     */
    public void invalidateOrganization(String organizationId) {
        invalidateAll();
        log.debug("Cache invalidated for organization {}", organizationId);
    }

    private static String key(String userId, String organizationId) {
        return "permission:" + userId + ":" + organizationId;
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
