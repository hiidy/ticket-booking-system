package com.seatwise.booking.messaging.rebalancer;

import com.seatwise.booking.messaging.BookingMessageConsumer;
import com.seatwise.booking.messaging.MessagingProperties;
import com.seatwise.booking.messaging.StreamKeyGenerator;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RebalanceCoordinator
    implements StreamListener<String, ObjectRecord<String, RebalanceMessage>> {

  private static final String ADMIN_LOCK_KEY = "lock:admin";
  private final String id = UUID.randomUUID().toString();
  private final MessagingProperties properties;
  private final RedissonClient redissonClient;
  private final RedisTemplate<String, Object> redisTemplate;
  private final RebalanceMessagePublisher publisher;
  private final StreamConsumerStateRepository consumerStateRepository;
  private final Map<String, StreamConsumerState> states = new HashMap<>();
  private final BookingMessageConsumer bookingMessageConsumer;
  private final StreamMessageListenerContainer<String, ObjectRecord<String, RebalanceMessage>>
      container;

  @PostConstruct
  public void initialize() throws InterruptedException {
    log.info("consumer {} 시작", id);
    lockAndCreateConsumerGroups();

    container.receive(StreamOffset.create("stream:consumer:updates", ReadOffset.latest()), this);
    container.start();

    joinConsumer(id);
  }

  public void joinConsumer(String consumerId) {
    rebalance(new RebalanceMessage(RebalanceType.JOIN, consumerId));
  }

  public void leaveConsumer(String consumerId) {
    rebalance(new RebalanceMessage(RebalanceType.LEAVE, consumerId));
  }

  private void rebalance(RebalanceMessage message) {
    RLock adminLock = redissonClient.getFairLock(ADMIN_LOCK_KEY);

    try {
      if (adminLock.tryLock(5, TimeUnit.SECONDS)) {

        // 레디스로부터 현재 활동중인 컨슈머 ID 불러오기
        states.clear();
        states.putAll(consumerStateRepository.getAllConsumerStates());

        if (message.rebalanceType().equals(RebalanceType.JOIN)) {
          if (!states.containsKey(message.requestedBy())) {
            states.put(
                message.requestedBy(),
                new StreamConsumerState(message.requestedBy(), Collections.emptyList()));
          }
        }

        if (message.rebalanceType().equals(RebalanceType.LEAVE)) {
          states.remove(message.requestedBy());
        }

        rebuildPartitionAssignments();

        consumerStateRepository.saveAllConsumerStates(states);

        publisher.publishUpdate(message);
      }

    } catch (InterruptedException e) {
      log.warn("admin lock 획득 실패 - 다른 컨슈머가 리밸런스 중");
    } finally {
      adminLock.unlock();
    }
  }

  private void rebuildPartitionAssignments() {
    Set<String> keys = states.keySet();
    int shardCount = properties.getShardCount();
    ConsistentHash<String> ch = new ConsistentHash<>(100, keys);

    Map<String, List<Integer>> newAssignments = new HashMap<>();

    for (String consumerId : keys) {
      newAssignments.put(consumerId, new ArrayList<>());
    }

    for (int i = 0; i < shardCount; i++) {
      String consumerId = ch.get(i);
      newAssignments.get(consumerId).add(i);
    }

    for (Map.Entry<String, List<Integer>> entry : newAssignments.entrySet()) {
      String consumerId = entry.getKey();
      List<Integer> newPartitions = entry.getValue();

      StreamConsumerState state = states.get(consumerId);
      if (state == null) {
        log.warn("예상치 못한 새 컨슈머: {}", consumerId);
        states.put(consumerId, new StreamConsumerState(consumerId, newPartitions));
      }
      state.updatePartitions(newPartitions);
    }

    log.info("파티션 재배치 완료:");
    for (Map.Entry<String, StreamConsumerState> entry : states.entrySet()) {
      String consumerId = entry.getKey();
      List<Integer> partitions = entry.getValue().getPartitions();
      log.info("컨슈머 {} -> 파티션 {}", consumerId, partitions);
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

  @Override
  public void onMessage(ObjectRecord<String, RebalanceMessage> message) {
    RebalanceMessage rebalanceMessage = message.getValue();
    log.info("RebalanceMessage 수신 : {}", rebalanceMessage);
    states.clear();
    states.putAll(consumerStateRepository.getAllConsumerStates());
    List<Integer> partitions = states.get(id).getPartitions();

    bookingMessageConsumer.updatePartitions(partitions);
  }
}
