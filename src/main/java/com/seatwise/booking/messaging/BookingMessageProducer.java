package com.seatwise.booking.messaging;

import com.seatwise.booking.dto.BookingMessage;
import com.seatwise.booking.exception.BookingException;
import com.seatwise.core.ErrorCode;
import com.seatwise.ticket.TicketCacheService;
import java.util.UUID;
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

  private final TicketCacheService cacheService;
  private final MessagingProperties messagingProperties;
  private final RedisTemplate<String, Object> redisTemplate;

  @Retryable(retryFor = RedisConnectionFailureException.class, backoff = @Backoff(delay = 1000))
  public void sendMessage(BookingMessage message) {
    if (cacheService.hasUnavailableTickets(message.ticketIds(), message.memberId())) {
      throw new BookingException(
          ErrorCode.SEAT_NOT_AVAILABLE, UUID.fromString(message.requestId()));
    }

    int partitionId =
        PartitionCalculator.calculatePartition(
            message.sectionId(), messagingProperties.getPartitionCount());

    String streamKey = StreamKeyGenerator.createStreamKey(partitionId);

    ObjectRecord<String, BookingMessage> objectRecord =
        StreamRecords.newRecord().in(streamKey).ofObject(message);

    redisTemplate
        .opsForStream(new Jackson2HashMapper(true))
        .add(objectRecord, XAddOptions.maxlen(1000).approximateTrimming(true));
  }
}
