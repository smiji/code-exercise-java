package com.tpx.urlshort.health;

import com.tpx.urlshort.cache.redis.UrlCacheService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class RedisHealthIndicator implements HealthIndicator {

    private final UrlCacheService urlCacheService;

    public RedisHealthIndicator(UrlCacheService urlCacheService) {
        this.urlCacheService = urlCacheService;
    }

    @Override
    public Health health() {
        try {
            // Test Redis connectivity by putting and getting a test value
            String testKey = "_health_check_" + System.currentTimeMillis();
            String testValue = "health_check";

            urlCacheService.put(testKey, testValue);
            var result = urlCacheService.get(testKey);

            if (result.isPresent() && result.get().equals(testValue)) {
                // Clean up the test key
                urlCacheService.evict(testKey);

                return Health.up().withDetail("cache", "Redis").withDetail("connection", "successful").build();
            } else {
                return Health.down().withDetail("cache", "Redis").withDetail("reason", "Test value mismatch").build();
            }
        } catch (Exception e) {
            return Health.down().withDetail("cache", "Redis").withException(e).build();
        }
    }
}
