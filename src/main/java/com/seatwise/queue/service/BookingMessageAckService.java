package com.seatwise.queue.service;

import com.seatwise.queue.dto.BookingMessage;
import com.seatwise.queue.dto.request.StreamMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingMessageAckService {

  private final RedisTemplate<String, Object> redisTemplate;
  private final StreamMessageService streamMessageService;

  @Retryable(retryFor = RedisSystemException.class)
  public void acknowledge(String group, ObjectRecord<String, BookingMessage> message) {
    redisTemplate.opsForStream().acknowledge(group, message);
  }

  @Recover
  public void recoverAcknowledge(
      RedisSystemException e, String group, ObjectRecord<String, BookingMessage> message) {
    log.warn("Ack 실패 복구 로직 실행 - streamName: {}, messageId: {}", group, message.getId(), e);
    streamMessageService.saveFailedStreamMessage(
        new StreamMessageRequest(message.getId().getValue(), message.getStream()));
  }
}
