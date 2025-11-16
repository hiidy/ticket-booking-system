package com.seatwise.booking.messaging;

import com.seatwise.booking.dto.BookingMessage;
import com.seatwise.booking.dto.BookingMessageType;
import com.seatwise.booking.exception.BookingException;
import com.seatwise.show.service.ShowBookingService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingMessageConsumer
    implements StreamListener<String, ObjectRecord<String, BookingMessage>> {

  private final StreamMessageListenerContainer<String, ObjectRecord<String, BookingMessage>>
      container;
  private final RedissonClient redissonClient;
  private final MessagingProperties properties;
  private final ShowBookingService showBookingService;
  private final BookingMessageAckService bookingMessageAckService;
  private final Map<Integer, Subscription> activeSubscriptions = new ConcurrentHashMap<>();
  private final Map<Integer, RLock> locks = new ConcurrentHashMap<>();
  private static final String LOCK_KEY = "lock:stream:";

  @Value("${spring.application.instance-id}")
  private String instanceId;

  public void updatePartitions(List<Integer> newPartitions) {
    log.info("컨슈머 재구독 시작: {} -> {}", activeSubscriptions.keySet(), newPartitions);
    HashSet<Integer> current = new HashSet<>(activeSubscriptions.keySet());
    HashSet<Integer> target = new HashSet<>(newPartitions);

    current.stream().filter(p -> !target.contains(p)).forEach(this::releasePartition);
    target.stream().filter(p -> !current.contains(p)).forEach(this::acquirePartition);
  }

  private void acquirePartition(Integer partitionId) {
    RLock acquireLock = redissonClient.getFairLock(LOCK_KEY + partitionId);
    if (acquireLock.tryLock()) {
      log.info("파티션 락 시도 : {}", partitionId);
      locks.put(partitionId, acquireLock);
      addSubscription(partitionId);
    }
  }

  private void releasePartition(Integer partitionId) {
    RLock lock = locks.remove(partitionId);
    if (lock != null) {
      try {
        if (lock.isHeldByCurrentThread()) {
          removeSubscription(partitionId);
          lock.unlock();
          log.info("다음 파티션 락 해제 : {}", partitionId);
        }
      } catch (Exception e) {
        log.error("파티션 락 해제 중 오류 발생: {}", partitionId, e);
      }
    }
  }

  private void addSubscription(Integer partitionId) {
    String streamKey = StreamKeyGenerator.createStreamKey(partitionId);
    String group = properties.getConsumerGroup();
    Subscription subscription =
        container.receive(
            Consumer.from(group, instanceId),
            StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
            this);
    activeSubscriptions.put(partitionId, subscription);
  }

  private void removeSubscription(Integer partitionId) {
    Subscription subscription = activeSubscriptions.remove(partitionId);
    container.remove(subscription);
  }

  @PostConstruct
  protected void init() {
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
        request.ticketIds(),
        request.sectionId());

    if (request.type() == BookingMessageType.BOOKING) {
      try {
        showBookingService.create(requestId, request.memberId(), request.ticketIds());
      } catch (BookingException e) {
        log.warn(
            "예약 실패: requestId={}, error={}, memberId={}",
            requestId,
            e.getBaseCode(),
            request.memberId());
        showBookingService.createFailedBooking(requestId, request.memberId());
      } finally {
        bookingMessageAckService.acknowledge(properties.getConsumerGroup(), message);
      }
    }

    if (request.type() == BookingMessageType.CLIENT_TIMEOUT_CANCEL) {
      showBookingService.cancelWithoutRefund(requestId);
    }
  }
}
