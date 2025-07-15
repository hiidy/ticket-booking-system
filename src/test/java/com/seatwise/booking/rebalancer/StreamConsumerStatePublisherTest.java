package com.seatwise.booking.rebalancer;

import static org.assertj.core.api.Assertions.*;

import com.seatwise.annotation.EmbeddedRedisTest;
import com.seatwise.booking.messaging.rebalancer.RebalanceRequest;
import com.seatwise.booking.messaging.rebalancer.RebalanceType;
import com.seatwise.booking.messaging.rebalancer.StreamConsumerStatePublisher;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;

@EmbeddedRedisTest
class StreamConsumerStatePublisherTest {

  private static final String STREAM_KEY = "stream:consumer:updates";
  @Autowired private StreamConsumerStatePublisher publisher;
  @Autowired private RedisTemplate<String, Object> redisTemplate;

  @Test
  void publishUpdate_shouldWriteToRedisStream() {
    // given
    RebalanceRequest request = new RebalanceRequest(RebalanceType.JOIN, "consumer-1");

    // when
    publisher.publishUpdate(request);

    // then
    List<MapRecord<String, Object, Object>> records =
        redisTemplate.opsForStream().read(StreamOffset.fromStart(STREAM_KEY));

    assertThat(records).isNotEmpty();

    MapRecord<String, Object, Object> record = records.get(0);
    Map<Object, Object> valueMap = record.getValue();

    assertThat(valueMap)
        .containsEntry("rebalanceType", "JOIN")
        .containsEntry("requestedBy", "consumer-1");
  }
}
