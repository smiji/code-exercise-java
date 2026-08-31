package com.tpx.urlshort.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class UrlRedisCacheService {

    private static final String PREFIX_URL = "url:";
    private final StringRedisTemplate redisTemplate;
    private final Duration ttlSeconds;

    public UrlRedisCacheService(StringRedisTemplate redisTemplate, long ttlSeconds) {
        this.redisTemplate = redisTemplate;
        this.ttlSeconds = Duration.ofSeconds(ttlSeconds);
    }

    public Optional<String> get(String alias) {
        validateAlias(alias);
        String value = redisTemplate.opsForValue().get(PREFIX_URL + alias.trim());
        return Optional.ofNullable(value);
    }

    public void put(String alias, String targetUrl) {
        validateAlias(alias);
        if (targetUrl == null || targetUrl.isBlank()) {
            throw new IllegalArgumentException("Target URL must not be null or empty");
        }

        redisTemplate.opsForValue().set(PREFIX_URL + alias.trim(), targetUrl, ttlSeconds);
    }

    public void evict(String alias) {
        validateAlias(alias);
        redisTemplate.delete(PREFIX_URL + alias);
    }

    void validateAlias(String alias) {
        if (alias == null || alias.isBlank()) {
            throw new IllegalArgumentException("Alias must not be null or empty");
        }
    }

}
