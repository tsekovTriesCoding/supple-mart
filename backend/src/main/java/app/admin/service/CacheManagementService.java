package app.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheManagementService {

    private final CacheManager cacheManager;

    public Map<String, CacheStats> getAllCacheStats() {
        Map<String, CacheStats> stats = new HashMap<>();

        cacheManager.getCacheNames().forEach(cacheName -> {
            CaffeineCache cache = (CaffeineCache) cacheManager.getCache(cacheName);
            if (cache != null) {
                com.github.benmanes.caffeine.cache.stats.CacheStats caffeineStats =
                        cache.getNativeCache().stats();

                stats.put(cacheName, new CacheStats(
                        cache.getNativeCache().estimatedSize(),
                        caffeineStats.hitCount(),
                        caffeineStats.missCount(),
                        caffeineStats.hitRate(),
                        caffeineStats.evictionCount()
                ));
            }
        });

        return stats;
    }

    public void clearCache(String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.info("Cache '{}' cleared", cacheName);
        } else {
            log.warn("Cache '{}' not found", cacheName);
        }
    }

    public void clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();
        });
        log.info("All caches cleared");
    }

    public Iterable<String> getCacheNames() {
        return cacheManager.getCacheNames();
    }

    public record CacheStats(
            long size,
            long hitCount,
            long missCount,
            double hitRate,
            long evictionCount
    ) {
    }
}
