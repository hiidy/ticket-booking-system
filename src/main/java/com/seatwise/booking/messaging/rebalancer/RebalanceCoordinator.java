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
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RebalanceCoordinator
    implements StreamListener<String, ObjectRecord<String, RebalanceMessage>> {

  private static final String ADMIN_LOCK_KEY = "lock:admin";

  @Value("${spring.application.instance-id:${random.uuid}}")
  private String instanceId;

  private final MessagingProperties properties;
  private final RedissonClient redissonClient;
  private final RebalanceMessageProducer producer;
  private final StreamConsumerStateRepository consumerStateRepository;
  private final Map<String, StreamConsumerState> states = new HashMap<>();
  private final BookingMessageConsumer bookingMessageConsumer;
  private final StreamMessageListenerContainer<String, ObjectRecord<String, RebalanceMessage>>
      container;
  private final ConsumerGroupInitializer groupInitializer;

  @PostConstruct
  public void initialize() {
    log.info("consumer {} 시작", instanceId);

    groupInitializer.initializeConsumerGroups();
    container.receive(
        StreamOffset.create(StreamKeyGenerator.getRebalanceUpdateKey(), ReadOffset.latest()), this);
    container.start();

    joinConsumer(instanceId);
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

        producer.sendRebalanceMessage(message);
      }

    } catch (InterruptedException e) {
      log.warn("admin lock 획득 실패 - 다른 컨슈머가 리밸런스 중");
    } finally {
      adminLock.unlock();
    }
  }

  private void rebuildPartitionAssignments() {
    Set<String> keys = states.keySet();
    int partitionCount = properties.getPartitionCount();
    ConsistentHash<String> ch = new ConsistentHash<>(100, keys);

    Map<String, List<Integer>> newAssignments = new HashMap<>();

    for (String consumerId : keys) {
      newAssignments.put(consumerId, new ArrayList<>());
    }

    for (int i = 0; i < partitionCount; i++) {
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

  @Override
  public void onMessage(ObjectRecord<String, RebalanceMessage> message) {
    RebalanceMessage rebalanceMessage = message.getValue();
    log.info("RebalanceMessage 수신 : {}", rebalanceMessage);
    states.clear();
    states.putAll(consumerStateRepository.getAllConsumerStates());
    List<Integer> partitions = states.get(instanceId).getPartitions();

    bookingMessageConsumer.updatePartitions(partitions);
  }
}
