package com.tpx.urlshort.cache.redis;

import java.util.Optional;

public interface UrlCacheService {

  Optional<String> get(String alias);

  void put(String alias, String actualUrl);

  void evict(String alias);
}
