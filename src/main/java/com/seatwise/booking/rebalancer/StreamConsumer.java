package com.seatwise.booking.rebalancer;

import com.seatwise.booking.messaging.MessagingProperties;
import com.seatwise.booking.messaging.StreamKeyGenerator;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StreamConsumer {

  private static final String ADMIN_LOCK_KEY = "lock:admin";
  private final String id = UUID.randomUUID().toString();
  private final MessagingProperties properties;
  private final RedissonClient redissonClient;
  private final RedisTemplate<String, Object> redisTemplate;
  private final StreamConsumerStateRepository consumerStateRepository;
  private final Map<String, List<Integer>> partitionAssignments = new HashMap<>();

  @PostConstruct
  public void initialize() {
    lockAndCreateConsumerGroups();
  }

  private void rebalance(RebalanceRequest request, String consumerId) {
    RLock adminLock = redissonClient.getLock(ADMIN_LOCK_KEY);

    // 레디스로부터 현재 활동중인 컨슈머 ID 불러오기
    Map<String, StreamConsumerState> consumerStates =
        consumerStateRepository.getAllConsumerStates();

    if (consumerStates.keySet().stream().anyMatch(key -> key.equals(consumerId))) {
      return;
    }

    if (request.rebalanceType().equals(RebalanceType.JOIN)) {
      partitionAssignments.putIfAbsent(request.requestedBy(), new ArrayList<>());
    }

    if (request.rebalanceType().equals(RebalanceType.LEAVE)) {
      partitionAssignments.remove(request.requestedBy());
    }
  }

  private void lockAndCreateConsumerGroups() {
    createConsumerGroups();
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
