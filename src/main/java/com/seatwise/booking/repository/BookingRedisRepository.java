package com.seatwise.booking.repository;

import com.seatwise.booking.dto.BookingResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookingRedisRepository {

  private final RedisTemplate<String, BookingResult> bookingResultRedisTemplate;
  private static final String BOOKING_RESULT_PREFIX = "booking:result:";

  public BookingResult getBookingResult(String requestId) {
    return bookingResultRedisTemplate.opsForValue().get(BOOKING_RESULT_PREFIX + requestId);
  }
}
