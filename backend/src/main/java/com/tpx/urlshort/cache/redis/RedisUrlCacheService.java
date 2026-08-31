package com.tpx.urlshort.cache.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class RedisUrlCacheService implements UrlCacheService {

  private static final Logger logger = LoggerFactory.getLogger(RedisUrlCacheService.class);

  private final StringRedisTemplate redisTemplate;
  private final Duration ttl;

  public RedisUrlCacheService(StringRedisTemplate redisTemplate,
      @Value("${app.cache.ttl-seconds:600}") long ttlSeconds) {
    this.redisTemplate = redisTemplate;
    this.ttl = Duration.ofSeconds(ttlSeconds);
  }

  @Override
  public Optional<String> get(String alias) {
    try {
      validateAlias(alias);
      String value = redisTemplate.opsForValue().get(buildKey(alias));
      return Optional.ofNullable(value);
    } catch (IllegalArgumentException e) {
      logger.warn("Redis cache get failed for alias '{}': {}", alias, e.getMessage());
      return Optional.empty();
    } catch (DataAccessException e) {
      logger.warn("Redis cache get failed for alias '{}': {}", alias, e.getMessage());
      return Optional.empty();
    }
  }

  @Override
  public void put(String alias, String actualUrl) {
    try {
      validateAlias(alias);
      if (actualUrl == null || actualUrl.isBlank()) {
        throw new IllegalArgumentException("Actual URL must not be null or empty");
      }
      redisTemplate.opsForValue().set(buildKey(alias), actualUrl, ttl);
    } catch (IllegalArgumentException e) {
      logger.warn("Redis cache put failed for alias '{}': {}", alias, e.getMessage());
    } catch (DataAccessException e) {
      logger.warn("Redis cache put failed for alias '{}': {}", alias, e.getMessage());
    }
  }

  @Override
  public void evict(String alias) {
    try {
      validateAlias(alias);
      redisTemplate.delete(buildKey(alias));
    } catch (IllegalArgumentException e) {
      logger.warn("Redis cache evict failed for alias '{}': {}", alias, e.getMessage());
    } catch (DataAccessException e) {
      logger.warn("Redis cache evict failed for alias '{}': {}", alias, e.getMessage());
    }
  }

  private String buildKey(String alias) {
    return "url:" + alias;
  }

  private void validateAlias(String alias) {
    if (alias == null || alias.isBlank()) {
      throw new IllegalArgumentException("Alias must not be null or empty");
    }
  }
}
