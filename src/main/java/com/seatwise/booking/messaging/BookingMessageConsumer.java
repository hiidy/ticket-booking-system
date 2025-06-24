package com.seatwise.booking.messaging;

import com.seatwise.booking.dto.BookingMessage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
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
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingMessageConsumer
    implements StreamListener<String, ObjectRecord<String, BookingMessage>> {

  private final StreamMessageListenerContainer<String, ObjectRecord<String, BookingMessage>>
      container;
  private final RedisTemplate<String, Object> redisTemplate;
  private final MessagingProperties properties;
  private final BookingMessageAckService bookingMessageAckService;
  private final BookingMessageHandler bookingMessageHandler;

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
    try {
      bookingMessageHandler.handleBookingMessage(request);
    } catch (Exception e) {
      log.warn("예매 도중 예외가 발생함 : {}", e.getMessage());
    } finally {
      bookingMessageAckService.acknowledge(properties.getConsumerGroup(), message);
    }
  }
}
