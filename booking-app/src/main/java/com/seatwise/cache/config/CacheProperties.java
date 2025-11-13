package com.seatwise.cache.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cache")
public record CacheProperties(
    LocalCacheProperties local, RedisCacheProperties redis, MultiLevelCacheProperties multiLevel) {
  public record LocalCacheProperties(
      boolean enabled,
      int maxSize,
      Duration ttl,  // 티켓 홀드 만료 시간 (쓰기 후)
      boolean recordStats) {}

  public record RedisCacheProperties(boolean enabled, Duration ticketHoldTtl) {}

  public record MultiLevelCacheProperties(
      boolean enabled, String invalidationChannel, boolean readThrough, boolean writeThrough) {}
}
