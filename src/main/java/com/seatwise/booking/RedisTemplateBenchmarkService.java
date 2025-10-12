package com.seatwise.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisTemplateBenchmarkService {

  private final StringRedisTemplate redisTemplate;

  public void setValue(String key, String value) {
    redisTemplate.opsForValue().set(key, value);
  }

  public String getValue(String key) {
    return redisTemplate.opsForValue().get(key);
  }
}
