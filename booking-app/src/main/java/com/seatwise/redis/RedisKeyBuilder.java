package com.seatwise.redis;

public class RedisKeyBuilder {

  public static String createRedisKey(RedisKeys redisKeys, Object... args) {
    return String.format(redisKeys.getKey(), args);
  }
}
