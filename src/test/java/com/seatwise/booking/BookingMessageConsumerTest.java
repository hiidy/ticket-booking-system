package com.seatwise.booking;

import static org.mockito.Mockito.*;

import com.seatwise.annotation.EmbeddedRedisTest;
import com.seatwise.booking.dto.BookingMessage;
import com.seatwise.booking.messaging.BookingMessageAckService;
import com.seatwise.booking.messaging.BookingMessageHandler;
import com.seatwise.booking.messaging.MessagingProperties;
import com.seatwise.booking.messaging.StreamKeyGenerator;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.hash.Jackson2HashMapper;

@SpringBootTest
@EmbeddedRedisTest
class BookingMessageConsumerTest {

  @Autowired private RedisTemplate<String, Object> objectRedisTemplate;
  @Autowired private MessagingProperties properties;

  @MockBean private BookingMessageHandler bookingMessageHandler;
  @MockBean private BookingMessageAckService bookingMessageAckService;

  private UUID requestId;
  private String streamKey;
  private String consumerGroup;

  @BeforeEach
  void setUp() {
    flushRedisDb();
    requestId = UUID.randomUUID();
    int shardId = 0;
    streamKey = StreamKeyGenerator.createStreamKey(shardId);
    consumerGroup = properties.getConsumerGroup();
  }

  private void flushRedisDb() {
    objectRedisTemplate.execute(
        (RedisConnection connection) -> {
          connection.serverCommands().flushAll();
          return "OK";
        });
  }

  @Test
  void shouldProcessValidMessageSuccessfully() {
    // given
    BookingMessage message = new BookingMessage(requestId.toString(), 1L, List.of(1L, 2L), 1L);
    ObjectRecord<String, BookingMessage> objectRecord =
        StreamRecords.newRecord().in(streamKey).ofObject(message);

    try {
      objectRedisTemplate.opsForStream().createGroup(streamKey, consumerGroup);
    } catch (Exception e) {
    }

    // when
    objectRedisTemplate
        .opsForStream(new Jackson2HashMapper(true))
        .add(objectRecord, XAddOptions.maxlen(1000).approximateTrimming(true));

    // then
    verify(bookingMessageHandler, timeout(Duration.ofSeconds(5).toMillis()))
        .handleBookingMessage(any(BookingMessage.class));
    verify(bookingMessageAckService, timeout(Duration.ofSeconds(1).toMillis()))
        .acknowledge(anyString(), any());
  }
}
