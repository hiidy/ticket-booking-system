package com.seatwise.booking.rebalancer;

import static org.assertj.core.api.Assertions.*;

import com.seatwise.annotation.EmbeddedRedisTest;
import com.seatwise.booking.messaging.rebalancer.RebalanceMessage;
import com.seatwise.booking.messaging.rebalancer.RebalanceMessageProducer;
import com.seatwise.booking.messaging.rebalancer.RebalanceType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;

@EmbeddedRedisTest
@Disabled
class RebalanceMessageProducerTest {

  private static final String STREAM_KEY = "rebalance:updates";
  @Autowired private RebalanceMessageProducer producer;
  @Autowired private RedisTemplate<String, Object> redisTemplate;

  @Test
  void sendRebalanceMessage_shouldWriteToRedisStream() {
    // given
    RebalanceMessage message = new RebalanceMessage(RebalanceType.JOIN, "consumer-1");

    // when
    producer.sendRebalanceMessage(message);

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
