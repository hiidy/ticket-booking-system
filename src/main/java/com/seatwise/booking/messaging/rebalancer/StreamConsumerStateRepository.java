package com.seatwise.booking.messaging.rebalancer;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StreamConsumerStateRepository {

  private static final String CONSUMER_KEY = "consumer:states";
  private final RedisTemplate<String, Object> redisTemplate;

  public void saveAllConsumerStates(Map<String, StreamConsumerState> consumerStates) {
    redisTemplate.opsForHash().putAll(CONSUMER_KEY, consumerStates);
  }

  public Map<String, StreamConsumerState> getAllConsumerStates() {
    Map<Object, Object> entries = redisTemplate.opsForHash().entries(CONSUMER_KEY);
    Map<String, StreamConsumerState> result = new HashMap<>();
    for (Map.Entry<Object, Object> entry : entries.entrySet()) {
      String key = entry.getKey().toString();
      StreamConsumerState state = (StreamConsumerState) entry.getValue();
      result.put(key, state);
    }
    return result;
  }
}
