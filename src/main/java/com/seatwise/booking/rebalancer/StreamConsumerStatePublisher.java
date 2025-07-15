package com.seatwise.booking.rebalancer;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.hash.Jackson2HashMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StreamConsumerStatePublisher {

  private final RedisTemplate<String, Object> redisTemplate;
  private static final String PUBLISH_KEY = "stream:consumer:updates";

  public void publishUpdate(RebalanceRequest request) {
    ObjectRecord<String, RebalanceRequest> objectRecord =
        StreamRecords.newRecord().in(PUBLISH_KEY).ofObject(request);
    redisTemplate
        .opsForStream(new Jackson2HashMapper(true))
        .add(objectRecord, XAddOptions.maxlen(1000).approximateTrimming(true));
  }
}
