package com.seatwise.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisCache {

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  public Boolean hasKey(String key) {
    return redisTemplate.hasKey(key);
  }

  public void putValue(String key, String value, long timeout, TimeUnit timeUnit) {
    redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
  }

  public void putHash(String key, String field, Object value, long timeout, TimeUnit timeUnit) {
    String serializedValue = serializeValue(value);
    redisTemplate.opsForHash().put(key, field, serializedValue);
    redisTemplate.expire(key, timeout, timeUnit);
  }

  public List<Object> hashValues(String key) {
    return redisTemplate.opsForHash().values(key);
  }

  private String serializeValue(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof String) {
      return (String) value;
    }
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("JSON 직렬화 실패", e);
    }
  }
}
