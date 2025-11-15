package com.seatwise.booking;

import com.seatwise.booking.dto.BookingCreateCommand;
import com.seatwise.booking.dto.BookingMessage;
import com.seatwise.booking.dto.BookingMessageType;
import com.seatwise.booking.exception.RecoverableBookingException;
import com.seatwise.booking.messaging.BookingMessageProducer;
import com.seatwise.show.cache.TicketCacheService;
import com.seatwise.core.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsyncBookingService {

  private final BookingMessageProducer producer;
  private final TicketCacheService cacheService;
  private final RedisTemplate<String, String> redisTemplate;
  private static final String KEY = "booking:request:";

  public UUID createBookingRequest(UUID idempotencyKey, BookingCreateCommand command) {
    String key = KEY + idempotencyKey;
    String existingRequestId = redisTemplate.opsForValue().get(key);
    if (existingRequestId != null) {
      return UUID.fromString(existingRequestId);
    }

    UUID requestId = UUID.randomUUID();

    if (cacheService.hasUnavailableTickets(command.ticketIds(), command.memberId())) {
      throw new RecoverableBookingException(ErrorCode.SEAT_NOT_AVAILABLE, requestId);
    }

    producer.sendMessage(
        new BookingMessage(
            BookingMessageType.BOOKING,
            requestId.toString(),
            command.memberId(),
            command.ticketIds(),
            command.sectionId()));
    return requestId;
  }
}
