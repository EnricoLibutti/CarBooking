package me.enrico.carbooking.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableCaching
@EnableScheduling
public class CachingConfig {

    private CacheManager cacheManager;

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
                new ConcurrentMapCache("cars"),
                new ConcurrentMapCache("occupiedCars"),
                new ConcurrentMapCache("futureBookings")
        ));
        this.cacheManager = cacheManager;
        return cacheManager;
    }

    @Scheduled(fixedRate = 60000)
    public void clearCaches() {
        if (cacheManager != null) {
            cacheManager.getCacheNames()
                    .forEach(cacheName -> cacheManager.getCache(cacheName).clear());
        }
    }
}