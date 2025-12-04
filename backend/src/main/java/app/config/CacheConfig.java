package app.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String PRODUCTS_CACHE = "products";
    public static final String PRODUCT_LISTS_CACHE = "productLists";
    public static final String CATEGORIES_CACHE = "categories";
    public static final String DASHBOARD_STATS_CACHE = "dashboardStats";
    public static final String USERS_CACHE = "users";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.setCacheNames(java.util.List.of(
                PRODUCTS_CACHE,
                PRODUCT_LISTS_CACHE,
                CATEGORIES_CACHE,
                DASHBOARD_STATS_CACHE,
                USERS_CACHE
        ));

        cacheManager.setCaffeine(defaultCacheBuilder());
        
        return cacheManager;
    }

    private Caffeine<Object, Object> defaultCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats();
    }

    @Bean
    public Caffeine<Object, Object> productsCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(200)
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .recordStats();
    }

    @Bean
    public Caffeine<Object, Object> categoriesCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(50)
                .expireAfterWrite(24, TimeUnit.HOURS)
                .recordStats();
    }

    @Bean
    public Caffeine<Object, Object> dashboardStatsCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(10)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats();
    }
}
