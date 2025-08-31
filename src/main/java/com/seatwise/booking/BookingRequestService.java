package com.seatwise.booking;

import com.seatwise.booking.dto.BookingCreateCommand;
import com.seatwise.booking.dto.BookingMessage;
import com.seatwise.booking.dto.BookingMessageType;
import com.seatwise.booking.messaging.BookingMessageProducer;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingRequestService {

  private final BookingMessageProducer producer;
  private final RedisTemplate<String, String> redisTemplate;
  private static final String KEY = "booking:request:";

  public String createBookingRequest(
      UUID idempotencyKey, BookingCreateCommand bookingCreateCommand) {

    String key = KEY + idempotencyKey.toString();
    String requestId = UUID.randomUUID().toString();

    Boolean success =
        redisTemplate.opsForValue().setIfAbsent(key, requestId, Duration.ofMinutes(30));
    if (Boolean.FALSE.equals(success)) {
      return redisTemplate.opsForValue().get(key);
    }

    producer.sendMessage(
        new BookingMessage(
            BookingMessageType.BOOKING,
            requestId,
            bookingCreateCommand.memberId(),
            bookingCreateCommand.ticketIds(),
            bookingCreateCommand.sectionId()));
    return requestId;
  }
}
