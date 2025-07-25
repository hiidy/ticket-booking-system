package com.seatwise.booking;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.seatwise.annotation.EmbeddedRedisTest;
import com.seatwise.booking.dto.BookingMessage;
import com.seatwise.booking.dto.response.BookingResponse;
import com.seatwise.booking.exception.BookingException;
import com.seatwise.booking.messaging.BookingMessageConsumer;
import com.seatwise.booking.messaging.StreamKeyGenerator;
import com.seatwise.core.ErrorCode;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
@EmbeddedRedisTest
class BookingMessageConsumerTest {

  @Autowired private BookingMessageConsumer consumer;
  @Autowired private RedisTemplate<String, Object> objectRedisTemplate;
  @Autowired private RedissonClient redissonClient;
  @MockBean private BookingService bookingService;

  private UUID requestId;
  private String streamKey;

  @BeforeEach
  void setUp() {
    flushRedisDb();
    requestId = UUID.randomUUID();
    int partitionId = 0;
    streamKey = StreamKeyGenerator.createStreamKey(partitionId);
  }

  private void flushRedisDb() {
    objectRedisTemplate.execute(
        (RedisConnection connection) -> {
          connection.serverCommands().flushAll();
          return "OK";
        });
  }

  @Test
  void processValidMessage_shouldCreateBooking() {
    // given
    Long memberId = 1L;
    List<Long> seatIds = List.of(1L, 2L);
    Long bookingId = 100L;
    BookingMessage message = new BookingMessage(requestId.toString(), memberId, seatIds, 1L);
    when(bookingService.createBooking(requestId, memberId, seatIds)).thenReturn(bookingId);

    ObjectRecord<String, BookingMessage> messageRecord =
        StreamRecords.newRecord().in(streamKey).ofObject(message);

    // when
    consumer.onMessage(messageRecord);

    // then
    verify(bookingService).createBooking(requestId, memberId, seatIds);

    BookingResponse expected = BookingResponse.success(bookingId, requestId);
    assertThat(expected).isNotNull();
    assertThat(expected.success()).isTrue();
    assertThat(expected.bookingId()).isEqualTo(bookingId);
    assertThat(expected.requestId()).isEqualTo(requestId);
  }

  @Test
  void processDuplicateMessage_shouldSkipBooking() {
    // given
    Long memberId = 1L;
    List<Long> seatIds = List.of(1L, 2L);
    BookingMessage message = new BookingMessage(requestId.toString(), memberId, seatIds, 1L);

    when(bookingService.createBooking(requestId, memberId, seatIds))
        .thenThrow(new BookingException(ErrorCode.DUPLICATE_IDEMPOTENCY_KEY, requestId));

    ObjectRecord<String, BookingMessage> messageRecord =
        StreamRecords.newRecord().in(streamKey).ofObject(message);

    // when
    consumer.onMessage(messageRecord);

    // then
    verify(bookingService).createBooking(requestId, memberId, seatIds);

    BookingResponse result = BookingResponse.failed(requestId);
    assertThat(result.success()).isFalse();
    assertThat(result.bookingId()).isNull();
    assertThat(result.requestId()).isEqualTo(requestId);
  }
}
