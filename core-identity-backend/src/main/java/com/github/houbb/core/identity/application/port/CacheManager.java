package com.github.houbb.core.identity.application.port;

import java.time.Duration;
import java.util.Optional;

/**
 * Cache manager abstraction (P7.2).
 * <p>
 * Provides a consistent cache interface regardless of backend:
 * Caffeine (in-process), Redis (distributed), or composite (Redis + Caffeine fallback).
 * <p>
 * Security note: never cache credentials, secrets, or full password hashes.
 * Only cache derived authorization results, JWKS keys, and config metadata.
 */
public interface CacheManager {

    /**
     * Retrieve a value from cache.
     *
     * @param key  the cache key
     * @param type the expected type of the cached value
     * @param <T>  the type
     * @return the cached value, or empty if not present
     */
    <T> Optional<T> get(String key, Class<T> type);

    /**
     * Store a value in cache.
     *
     * @param key   the cache key
     * @param value the value to cache
     * @param ttl   time-to-live
     */
    void put(String key, Object value, Duration ttl);

    /**
     * Remove a specific key from cache.
     */
    void invalidate(String key);

    /**
     * Invalidate keys matching a pattern (e.g. "permission:*:org123:*").
     * Support depends on backend:
     * - Redis: SCAN + DEL by pattern (atomic per-node)
     * - Caffeine: invalidateAll (broad invalidation, not pattern-aware)
     *
     * @param pattern the key pattern to invalidate
     */
    void invalidateByPattern(String pattern);

    /**
     * Clear all entries. Use sparingly — only for emergency cache resets.
     */
    void invalidateAll();
}
