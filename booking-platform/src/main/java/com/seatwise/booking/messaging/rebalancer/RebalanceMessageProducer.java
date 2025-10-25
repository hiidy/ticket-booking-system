package com.seatwise.booking.messaging.rebalancer;

import com.seatwise.booking.messaging.StreamKeyGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.hash.Jackson2HashMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RebalanceMessageProducer {

  private final RedisTemplate<String, Object> redisTemplate;

  public void sendRebalanceMessage(RebalanceMessage message) {
    ObjectRecord<String, RebalanceMessage> objectRecord =
        StreamRecords.newRecord().in(StreamKeyGenerator.getRebalanceUpdateKey()).ofObject(message);
    redisTemplate
        .opsForStream(new Jackson2HashMapper(true))
        .add(objectRecord);
  }
}
