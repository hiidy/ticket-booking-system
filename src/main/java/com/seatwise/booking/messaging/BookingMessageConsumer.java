package com.seatwise.booking.messaging;

import com.seatwise.booking.BookingResultDispatcher;
import com.seatwise.booking.BookingService;
import com.seatwise.booking.dto.BookingMessage;
import com.seatwise.booking.dto.BookingResult;
import com.seatwise.booking.exception.BookingException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingMessageConsumer
    implements StreamListener<String, ObjectRecord<String, BookingMessage>> {

  private final StreamMessageListenerContainer<String, ObjectRecord<String, BookingMessage>>
      container;
  private final RedisTemplate<String, Object> redisTemplate;
  private final StringRedisTemplate stringRedisTemplate;
  private final MessagingProperties properties;
  private final BookingService bookingService;
  private final BookingResultDispatcher waitService;
  private final BookingMessageAckService bookingMessageAckService;
  private final List<Subscription> activeSubscriptions = new ArrayList<>();

  @Value("${spring.application.instance-idx}")
  private int instanceIdx;

  @PostConstruct
  protected void init() {
    configureSubscription(properties.getInstanceCount());
    container.start();
  }

  @PreDestroy
  protected void destroy() {
    if (container != null) {
      container.stop();
    }
  }

  @Scheduled(fixedRate = 10000)
  public void fetchInstanceCount() {
    String countStr = stringRedisTemplate.opsForValue().get("instance_count");
    if (countStr != null) {
      int instanceCount = Integer.parseInt(countStr);
      if (instanceCount != properties.getInstanceCount()) {
        log.info("인스턴스 개수 변화 감지 : {}개로 변경", instanceCount);
        configureSubscription(instanceCount);
        properties.setInstanceCount(instanceCount);
      }
    }
  }

  private void configureSubscription(int instanceCount) {
    for (Subscription s : activeSubscriptions) {
      s.cancel();
    }
    activeSubscriptions.clear();

    int shardCount = properties.getShardCount();
    String group = properties.getConsumerGroup();

    for (int shardId = 0; shardId < shardCount; shardId++) {
      if (shardId % instanceCount == instanceIdx % instanceCount) {
        String streamKey = StreamKeyGenerator.createStreamKey(shardId);

        try {
          redisTemplate.opsForStream().createGroup(streamKey, group);
        } catch (Exception e) {
          log.info("해당 스트림키에 대해서 그룹이 이미 존재합니다 : {}", streamKey);
        }

        Subscription subscription =
            container.receive(
                Consumer.from(group, String.valueOf(instanceIdx)),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                this);
        activeSubscriptions.add(subscription);
      }
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
