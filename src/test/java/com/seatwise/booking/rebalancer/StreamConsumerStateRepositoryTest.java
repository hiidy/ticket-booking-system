package com.seatwise.booking.rebalancer;

import static org.assertj.core.api.Assertions.*;

import com.seatwise.annotation.EmbeddedRedisTest;
import com.seatwise.booking.messaging.rebalancer.StreamConsumerState;
import com.seatwise.booking.messaging.rebalancer.StreamConsumerStateRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

@EmbeddedRedisTest
class StreamConsumerStateRepositoryTest {

  private final String STATE_KEY = "consumer:states";
  @Autowired private StreamConsumerStateRepository repository;
  @Autowired private RedisTemplate<String, Object> redisTemplate;

  @BeforeEach
  void flushRedis() {
    redisTemplate.getConnectionFactory().getConnection().flushAll();
  }

  @Test
  void saveAllConsumerStates_ShouldStoreAllEntries() {
    // given
    String consumerId1 = UUID.randomUUID().toString();
    String consumerId2 = UUID.randomUUID().toString();

    StreamConsumerState state1 = new StreamConsumerState(consumerId1, List.of(0, 1));
    StreamConsumerState state2 = new StreamConsumerState(consumerId2, List.of(2, 3));

    Map<String, StreamConsumerState> states =
        Map.of(
            consumerId1, state1,
            consumerId2, state2);

    // when
    repository.saveAllConsumerStates(states);

    // then
    Map<String, StreamConsumerState> stored = repository.getAllConsumerStates();

    assertThat(stored).hasSize(2);
    assertThat(stored.get(consumerId1).getPartitions()).isEqualTo(state1.getPartitions());
    assertThat(stored.get(consumerId2).getPartitions()).isEqualTo(state2.getPartitions());
  }

  @Test
  void getAllConsumerStates_ShouldReturnAllConsumers() {
    // given
    String consumerId = UUID.randomUUID().toString();
    List<Integer> partitions = List.of(1, 2);
    StreamConsumerState consumerState = new StreamConsumerState(consumerId, partitions);
    redisTemplate.opsForHash().put(STATE_KEY, consumerId, consumerState);

    // when
    Map<String, StreamConsumerState> consumerStates = repository.getAllConsumerStates();

    // then
    assertThat(consumerStates.keySet()).hasSize(1);
  }
}
