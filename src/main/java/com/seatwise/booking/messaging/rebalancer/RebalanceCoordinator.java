package com.seatwise.booking.messaging.rebalancer;

import com.seatwise.booking.messaging.BookingMessageConsumer;
import com.seatwise.booking.messaging.MessagingProperties;
import com.seatwise.booking.messaging.StreamKeyGenerator;
import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RebalanceCoordinator
    implements StreamListener<String, ObjectRecord<String, RebalanceMessage>> {

  private static final String ADMIN_LOCK_KEY = "lock:admin";
  private final String id = UUID.randomUUID().toString();
  private final MessagingProperties properties;
  private final RedissonClient redissonClient;
  private final RedisTemplate<String, Object> redisTemplate;
  private final RebalanceEventPublisher publisher;
  private final StreamConsumerStateRepository consumerStateRepository;
  private final Map<String, StreamConsumerState> states = new HashMap<>();
  private final BookingMessageConsumer bookingMessageConsumer;

  @PostConstruct
  public void initialize() {
    lockAndCreateConsumerGroups();
  }

  private void rebalance(RebalanceMessage message) {
    if (message.requestedBy().equals(id)) {
      return;
    }

    RLock adminLock = redissonClient.getLock(ADMIN_LOCK_KEY);

    // 레디스로부터 현재 활동중인 컨슈머 ID 불러오기
    states.clear();
    states.putAll(consumerStateRepository.getAllConsumerStates());

    if (message.rebalanceType().equals(RebalanceType.JOIN)) {
      joinConsumer(message);
    }

    if (message.rebalanceType().equals(RebalanceType.LEAVE)) {
      leaveConsumer(message);
    }

    rebuildPartitionAssignments();

    consumerStateRepository.saveAllConsumerStates(states);

    publisher.publishUpdate(message);
  }

  private void joinConsumer(RebalanceMessage message) {
    if (!states.containsKey(message.requestedBy())) {
      states.put(
          message.requestedBy(),
          new StreamConsumerState(message.requestedBy(), Collections.emptyList()));
    }
  }

  private void leaveConsumer(RebalanceMessage message) {
    states.remove(message.requestedBy());
  }

  private void rebuildPartitionAssignments() {}

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

  @Override
  public void onMessage(ObjectRecord<String, RebalanceMessage> message) {
    RebalanceMessage rebalanceMessage = message.getValue();
    rebalance(rebalanceMessage);

    List<Integer> partitions = states.get(id).partitions();

    bookingMessageConsumer.updatePartitions(partitions);
  }
}
