package com.seatwise.booking.rebalancer;

import com.seatwise.booking.messaging.MessagingProperties;
import com.seatwise.booking.messaging.StreamKeyGenerator;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StreamProcessor {

  private final MessagingProperties properties;
  private final RedissonClient redissonClient;
  private final RedisTemplate<String, Object> redisTemplate;
  private boolean isLeader = false;

  @PostConstruct
  public void initialize() {
    lockAndCreateConsumerGroups();
  }

  private void lockAndCreateConsumerGroups() {
    String lockKey = "lock:admin";
    RLock lock = redissonClient.getLock(lockKey);

    try {
      if (lock.tryLock(30, 120, TimeUnit.SECONDS)) {
        createConsumerGroups();
      }
    } catch (InterruptedException e) {
      lock.unlock();
    }
  }

  private void createConsumerGroups() {
    int shardCount = properties.getShardCount();
    String group = properties.getConsumerGroup();

    for (int shardId = 0; shardId < shardCount; shardId++) {
      String streamKey = StreamKeyGenerator.createStreamKey(shardId);
      try {
        redisTemplate.opsForStream().createGroup(streamKey, group);
      } catch (Exception e) {
        log.info("해당 스트림키에 대해서 그룹이 이미 존재합니다 : {}", streamKey);
      }
    }
  }
}
