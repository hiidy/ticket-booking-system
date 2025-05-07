package com.seatwise.booking.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.seatwise.annotation.EmbeddedRedisTest;
import com.seatwise.booking.dto.BookingResult;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@EmbeddedRedisTest
@SpringBootTest
class BookingRedisRepositoryTest {

  @Autowired private BookingRedisRepository bookingRedisRepository;
  @Autowired private RedisTemplate<String, Object> redisTemplate;

  @Test
  void givenPrefixAndRequestId_whenGetBookingResultFromRedis_thenReturnSavedResult() {
    // given
    UUID requestId = UUID.randomUUID();
    String prefix = "booking:result:";
    Long bookingId = 1L;
    BookingResult result = new BookingResult(true, bookingId, requestId);
    redisTemplate.opsForValue().set(prefix + requestId, result);

    // when
    BookingResult bookingResult = bookingRedisRepository.getBookingResult(requestId);

    // then
    assertThat(bookingResult.bookingId()).isEqualTo(bookingId);
    assertThat(bookingResult.requestId()).isEqualTo(requestId);
    assertThat(bookingResult.success()).isTrue();
  }
}
