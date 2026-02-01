package app.admin.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheManagementServiceTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private CaffeineCache caffeineCache;

    @Mock
    private Cache<Object, Object> nativeCache;

    private CacheManagementService cacheManagementService;

    @BeforeEach
    void setUp() {
        cacheManagementService = new CacheManagementService(cacheManager);
    }

    @Nested
    @DisplayName("Get All Cache Stats Tests")
    class GetAllCacheStatsTests {

        @Test
        @DisplayName("Should return stats for all caches")
        void shouldReturnStatsForAllCaches() {
            Collection<String> cacheNames = List.of("products", "users");
            CacheStats mockStats = CacheStats.of(100, 10, 0, 0, 0, 0, 0);

            when(cacheManager.getCacheNames()).thenReturn(cacheNames);
            when(cacheManager.getCache("products")).thenReturn(caffeineCache);
            when(cacheManager.getCache("users")).thenReturn(caffeineCache);
            when(caffeineCache.getNativeCache()).thenReturn(nativeCache);
            when(nativeCache.estimatedSize()).thenReturn(50L);
            when(nativeCache.stats()).thenReturn(mockStats);

            Map<String, CacheManagementService.CacheStats> result = cacheManagementService.getAllCacheStats();

            assertThat(result).hasSize(2);
            assertThat(result).containsKeys("products", "users");
        }

        @Test
        @DisplayName("Should return empty map when no caches")
        void shouldReturnEmptyMapWhenNoCaches() {
            when(cacheManager.getCacheNames()).thenReturn(List.of());

            Map<String, CacheManagementService.CacheStats> result = cacheManagementService.getAllCacheStats();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle null cache gracefully")
        void shouldHandleNullCacheGracefully() {
            when(cacheManager.getCacheNames()).thenReturn(List.of("products"));
            when(cacheManager.getCache("products")).thenReturn(null);

            Map<String, CacheManagementService.CacheStats> result = cacheManagementService.getAllCacheStats();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Clear Cache Tests")
    class ClearCacheTests {

        @Test
        @DisplayName("Should clear specific cache")
        void shouldClearSpecificCache() {
            when(cacheManager.getCache("products")).thenReturn(caffeineCache);

            cacheManagementService.clearCache("products");

            verify(caffeineCache).clear();
        }

        @Test
        @DisplayName("Should handle non-existent cache gracefully")
        void shouldHandleNonExistentCache() {
            when(cacheManager.getCache("nonexistent")).thenReturn(null);

            cacheManagementService.clearCache("nonexistent");

            verify(cacheManager).getCache("nonexistent");
        }
    }

    @Nested
    @DisplayName("Clear All Caches Tests")
    class ClearAllCachesTests {

        @Test
        @DisplayName("Should clear all caches")
        void shouldClearAllCaches() {
            when(cacheManager.getCacheNames()).thenReturn(List.of("products", "users"));
            when(cacheManager.getCache("products")).thenReturn(caffeineCache);
            when(cacheManager.getCache("users")).thenReturn(caffeineCache);

            cacheManagementService.clearAllCaches();

            verify(caffeineCache, times(2)).clear();
        }
    }

    @Nested
    @DisplayName("Get Cache Names Tests")
    class GetCacheNamesTests {

        @Test
        @DisplayName("Should return all cache names")
        void shouldReturnAllCacheNames() {
            Collection<String> expectedNames = List.of("products", "users", "orders");
            when(cacheManager.getCacheNames()).thenReturn(expectedNames);

            Iterable<String> result = cacheManagementService.getCacheNames();

            assertThat(result).containsExactlyElementsOf(expectedNames);
        }
    }

    @Nested
    @DisplayName("Cache Stats Record Tests")
    class CacheStatsRecordTests {

        @Test
        @DisplayName("Should create CacheStats record correctly")
        void shouldCreateCacheStatsRecordCorrectly() {
            CacheManagementService.CacheStats stats = new CacheManagementService.CacheStats(
                    100L, 80L, 20L, 0.8, 5L
            );

            assertThat(stats.size()).isEqualTo(100L);
            assertThat(stats.hitCount()).isEqualTo(80L);
            assertThat(stats.missCount()).isEqualTo(20L);
            assertThat(stats.hitRate()).isEqualTo(0.8);
            assertThat(stats.evictionCount()).isEqualTo(5L);
        }
    }
}
