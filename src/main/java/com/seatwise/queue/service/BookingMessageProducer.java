package com.seatwise.queue.service;

import com.seatwise.queue.QueueProperties;
import com.seatwise.queue.StreamKeyGenerator;
import com.seatwise.queue.dto.BookingMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.hash.Jackson2HashMapper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingMessageProducer {

  private final QueueProperties queueProperties;
  private final RedisTemplate<String, Object> redisTemplate;

  @Retryable(retryFor = RedisConnectionFailureException.class, backoff = @Backoff(delay = 1000))
  public void sendMessage(BookingMessage message) {
    String streamKey =
        StreamKeyGenerator.forSectionShard(message.sectionId(), queueProperties.getShardCount());
    ObjectRecord<String, BookingMessage> objectRecord =
        StreamRecords.newRecord().in(streamKey).ofObject(message);
    redisTemplate
        .opsForStream(new Jackson2HashMapper(true))
        .add(objectRecord, XAddOptions.maxlen(1000).approximateTrimming(true));
  }
}
