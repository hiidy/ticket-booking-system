package com.seatwise.booking.messaging.rebalancer;

import com.seatwise.booking.messaging.MessagingProperties;
import com.seatwise.booking.messaging.StreamKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumerGroupInitializer {

  private final MessagingProperties properties;
  private final RedisTemplate<String, Object> redisTemplate;

  public void initializeConsumerGroups() {
    String consumerGroup = properties.getConsumerGroup();
    int partitionCount = properties.getPartitionCount();

    log.info("컨슈머 그룹 초기화 시작: {} (파티션 수: {})", consumerGroup, partitionCount);

    for (int partitionId = 0; partitionId < partitionCount; partitionId++) {
      String streamKey = StreamKeyGenerator.createStreamKey(partitionId);
      createConsumerGroupIfNotExists(streamKey, consumerGroup);
    }

    log.info("컨슈머 그룹 초기화 완료");
  }

  private void createConsumerGroupIfNotExists(String streamKey, String consumerGroup) {
    try {
      redisTemplate.opsForStream().createGroup(streamKey, consumerGroup);
      log.debug("컨슈머 그룹 생성 성공: {} - {}", streamKey, consumerGroup);
    } catch (Exception e) {
      log.debug("컨슈머 그룹이 이미 존재함: {} - {}", streamKey, consumerGroup);
    }
  }
}
