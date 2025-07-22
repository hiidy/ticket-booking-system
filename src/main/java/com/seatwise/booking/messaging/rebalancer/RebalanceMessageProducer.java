package com.seatwise.booking.messaging.rebalancer;

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
  private static final String REBALANCE_STREAM_KEY = "rebalance:updates";

  public void sendRebalanceMessage(RebalanceMessage message) {
    ObjectRecord<String, RebalanceMessage> objectRecord =
        StreamRecords.newRecord().in(REBALANCE_STREAM_KEY).ofObject(message);
    redisTemplate
        .opsForStream(new Jackson2HashMapper(true))
        .add(objectRecord, XAddOptions.maxlen(1000).approximateTrimming(true));
  }
}
