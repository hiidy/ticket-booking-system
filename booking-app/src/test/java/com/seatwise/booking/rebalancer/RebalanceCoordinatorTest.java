package com.seatwise.booking.rebalancer;

import static org.assertj.core.api.Assertions.assertThat;

import com.seatwise.booking.messaging.MessagingProperties;
import com.seatwise.booking.messaging.StreamKeyGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.core.RedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest
class RebalanceCoordinatorTest {

  @Container @ServiceConnection
  static final GenericContainer<?> REDIS_CONTAINER =
      new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

  @Autowired private MessagingProperties properties;
  @Autowired private RedisTemplate<String, Object> redisTemplate;

  @Test
  void shouldCreateConsumerGroupsOnStartUp() {
    int partitionCount = properties.getPartitionCount();
    String consumerGroup = properties.getConsumerGroup();

    for (int partitionId = 0; partitionId < partitionCount; partitionId++) {
      String streamKey = StreamKeyGenerator.createStreamKey(partitionId);
      StreamInfo.XInfoGroups groups = redisTemplate.opsForStream().groups(streamKey);
      assertThat(groups).hasSize(1);
      assertThat(groups.get(0).groupName()).isEqualTo(consumerGroup);
    }
  }
}
