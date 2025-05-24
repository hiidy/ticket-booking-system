package com.seatwise.queue.service;

import com.seatwise.booking.dto.BookingResult;
import com.seatwise.booking.service.BookingResultWaitService;
import com.seatwise.booking.service.BookingService;
import com.seatwise.common.exception.BookingException;
import com.seatwise.queue.QueueProperties;
import com.seatwise.queue.StreamKeyGenerator;
import com.seatwise.queue.dto.BookingMessage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingMessageConsumer
    implements StreamListener<String, ObjectRecord<String, BookingMessage>> {

  private final StreamMessageListenerContainer<String, ObjectRecord<String, BookingMessage>>
      container;
  private final RedisTemplate<String, Object> redisTemplate;
  private final QueueProperties properties;
  private final BookingService bookingService;
  private final BookingResultWaitService waitService;
  private final BookingMessageAckService bookingMessageAckService;

  @Value("${spring.application.instance-idx}")
  private int instanceIdx;

  @PostConstruct
  protected void init() {
    int shardCount = properties.getShardCount();
    int instanceCount = properties.getInstanceCount();
    String group = properties.getConsumerGroup();

    for (int shardId = 0; shardId < shardCount; shardId++) {
      if (shardId % instanceCount == instanceIdx % instanceCount) {
        String streamKey = StreamKeyGenerator.createStreamKey(shardId);

        try {
          redisTemplate.opsForStream().createGroup(streamKey, group);
        } catch (Exception e) {
          log.info("해당 스트림키에 대해서 그룹이 이미 존재합니다 : {}", streamKey);
        }

        container.receive(
            Consumer.from(group, String.valueOf(instanceIdx)),
            StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
            this);
      }
    }
    container.start();
  }

  @PreDestroy
  protected void destroy() {
    if (container != null) {
      container.stop();
    }
  }

  @Override
  public void onMessage(ObjectRecord<String, BookingMessage> message) {
    BookingMessage request = message.getValue();
    UUID requestId = UUID.fromString(request.requestId());
    log.info(
        "멤버Id: {}, 좌석Id: {}, 섹션Id: {}에 대한 요청 처리중",
        request.memberId(),
        request.showSeatIds(),
        request.sectionId());
    try {
      Long bookingId =
          bookingService.createBooking(requestId, request.memberId(), request.showSeatIds());
      BookingResult result = BookingResult.success(bookingId, requestId);
      waitService.completeResult(requestId, result);
    } catch (BookingException e) {
      waitService.completeWithFailure(requestId, e);
    } finally {
      bookingMessageAckService.acknowledge(properties.getConsumerGroup(), message);
    }
  }
}
