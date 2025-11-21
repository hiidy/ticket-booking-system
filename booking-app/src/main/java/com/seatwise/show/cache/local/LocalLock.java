package com.seatwise.show.cache.local;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.concurrent.locks.ReentrantLock;

public class LocalLock {

  private final Cache<String, ReentrantLock> lockCache;

  public LocalLock() {
    this(Duration.ofHours(1));
  }

  public LocalLock(Duration expireTime) {
    this.lockCache = Caffeine.newBuilder().expireAfterWrite(expireTime).build();
  }

  public ReentrantLock getLock(String lockKey) {
    return lockCache.get(lockKey, k -> new ReentrantLock());
  }
}
