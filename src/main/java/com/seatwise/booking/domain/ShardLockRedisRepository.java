package com.seatwise.booking.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ShardLockRedisRepository {

  private static final String KEY = "shard-lock:";
  private final StringRedisTemplate redisTemplate;

  public boolean tryLockShard(int shardId, int instanceId) {
    return Boolean.TRUE.equals(
        redisTemplate.opsForValue().setIfAbsent(KEY + shardId, String.valueOf(instanceId)));
  }

  public boolean unlockShardIfOwned(int shardId, int instanceId) {
    String currentOwner = redisTemplate.opsForValue().get(KEY + shardId);
    if (String.valueOf(instanceId).equals(currentOwner)) {
      redisTemplate.delete(KEY + shardId);
      return true;
    }
    return false;
  }
}
