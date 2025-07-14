package com.seatwise.booking.rebalancer;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class StreamConsumerStateRepositoryTest {

  private final String STATE_KEY = "consumer:booking";
  @Autowired private StreamConsumerStateRepository repository;
  @Autowired private RedisTemplate<String, Object> redisTemplate;

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
