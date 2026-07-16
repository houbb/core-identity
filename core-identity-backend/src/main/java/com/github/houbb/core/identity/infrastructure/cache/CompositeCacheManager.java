package com.github.houbb.core.identity.infrastructure.cache;

import com.github.houbb.core.identity.application.port.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Composite cache manager (P7.2).
 * <p>
 * Multi-tier caching strategy:
 * 1. Try L1 (Caffeine, in-process) — fastest
 * 2. Try L2 (Redis, distributed) — shared across nodes
 * 3. Fall back to DB query (caller responsibility)
 * <p>
 * Invalidations propagate to all tiers.
 */
public class CompositeCacheManager implements CacheManager {

    private static final Logger log = LoggerFactory.getLogger(CompositeCacheManager.class);

    private final List<CacheManager> tiers;
    private final CacheManager primary;
    private final CacheManager fallback;

    /**
     * Create a composite with L1 (Caffeine) + L2 (Redis).
     */
    public CompositeCacheManager(CacheManager primary, CacheManager fallback) {
        this.primary = primary;
        this.fallback = fallback;
        this.tiers = new ArrayList<>();
        this.tiers.add(primary);
        if (fallback != null) {
            this.tiers.add(fallback);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        // Try primary tier first
        Optional<T> result = primary.get(key, type);
        if (result.isPresent()) {
            log.debug("Composite cache hit (L1): {}", key);
            return result;
        }

        // Try fallback tier
        if (fallback != null) {
            result = fallback.get(key, type);
            if (result.isPresent()) {
                log.debug("Composite cache hit (L2): {}", key);
                // Warm up primary tier
                primary.put(key, result.get(), Duration.ofMinutes(5));
                return result;
            }
        }

        log.debug("Composite cache miss: {}", key);
        return Optional.empty();
    }

    @Override
    public void put(String key, Object value, Duration ttl) {
        for (CacheManager tier : tiers) {
            try {
                tier.put(key, value, ttl);
            } catch (Exception e) {
                log.warn("Failed to put in cache tier {}: {}", tier.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    @Override
    public void invalidate(String key) {
        for (CacheManager tier : tiers) {
            try {
                tier.invalidate(key);
            } catch (Exception e) {
                log.warn("Failed to invalidate in cache tier {}: {}", tier.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    @Override
    public void invalidateByPattern(String pattern) {
        for (CacheManager tier : tiers) {
            try {
                tier.invalidateByPattern(pattern);
            } catch (Exception e) {
                log.warn("Failed to pattern-invalidate in cache tier {}: {}", tier.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    @Override
    public void invalidateAll() {
        for (CacheManager tier : tiers) {
            try {
                tier.invalidateAll();
            } catch (Exception e) {
                log.warn("Failed to clear cache tier {}: {}", tier.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    /**
     * Returns true if the Redis (L2) tier is available.
     */
    public boolean isRedisAvailable() {
        return fallback != null;
    }
}
