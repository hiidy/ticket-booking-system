package com.seatwise.queue.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.seatwise.annotation.EmbeddedRedisTest;
import com.seatwise.booking.dto.BookingResult;
import com.seatwise.booking.service.BookingService;
import com.seatwise.queue.StreamKeyGenerator;
import com.seatwise.queue.dto.BookingMessage;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
  @Autowired private RedisTemplate<String, BookingResult> redisTemplate;
  @Autowired private RedisTemplate<String, Object> objectRedisTemplate;
  @MockBean private BookingService bookingService;
  private String requestId;
  private String streamKey;

  @BeforeEach
  void setUp() {
    flushRedisDb();
    requestId = "testRequestId" + System.currentTimeMillis();
    int shardId = 0;
    streamKey = StreamKeyGenerator.createStreamKey(shardId);
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private void flushRedisDb() {
    objectRedisTemplate.execute(
        (RedisConnection connection) -> {
          connection.serverCommands().flushAll();
          return "OK";
        });
  }

  @Test
  void givenValidMessage_whenInvokeOnMessage_thenSuccess() {
    // given
    Long memberId = 1L;
    List<Long> showSeats = List.of(1L, 2L);
    Long sectionId = 1L;
    Long expectedBookingId = 100L;

    BookingMessage message = new BookingMessage(requestId, memberId, showSeats, sectionId);

    when(bookingService.createBooking(memberId, showSeats)).thenReturn(expectedBookingId);
    ObjectRecord<String, BookingMessage> objectRecord =
        StreamRecords.newRecord().in(streamKey).ofObject(message);

    // when
    consumer.onMessage(objectRecord);

    // then
    BookingResult result = redisTemplate.opsForValue().get("booking:result:" + requestId);

    assertThat(result).isNotNull();
    assertThat(result.success()).isTrue();
    assertThat(result.bookingId()).isEqualTo(expectedBookingId);
    assertThat(result.requestId()).isEqualTo(requestId);
    verify(bookingService, times(1)).createBooking(memberId, showSeats);
  }

  @Test
  void givenInValidMessage_whenInvokeOnMessage_thenFailed() {
    // given
    Long memberId = 1L;
    List<Long> showSeats = List.of(1L, 2L);
    Long sectionId = 1L;

    BookingMessage message = new BookingMessage(requestId, memberId, showSeats, sectionId);

    when(bookingService.createBooking(memberId, showSeats)).thenThrow(new RuntimeException());
    ObjectRecord<String, BookingMessage> objectRecord =
        StreamRecords.newRecord().in(streamKey).ofObject(message);

    // when
    consumer.onMessage(objectRecord);

    // then
    BookingResult result = redisTemplate.opsForValue().get("booking:result:" + requestId);

    assertThat(result).isNotNull();
    assertThat(result.success()).isFalse();
    assertThat(result.bookingId()).isNull();
    assertThat(result.requestId()).isEqualTo(requestId);
    verify(bookingService, times(1)).createBooking(memberId, showSeats);
  }
}
