package com.seatwise.booking;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.seatwise.booking.dto.BookingMessage;
import com.seatwise.booking.dto.BookingMessageType;
import com.seatwise.booking.messaging.BookingMessageProducer;
import com.seatwise.booking.messaging.MessagingProperties;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.hash.Jackson2HashMapper;

@ExtendWith(MockitoExtension.class)
class BookingMessageProducerTest {

  @Mock private RedisTemplate<String, Object> redisTemplate;
  @Mock private MessagingProperties messagingProperties;
  @InjectMocks private BookingMessageProducer bookingMessageProducer;

  @Test
  void shouldSendBookingMessageToCorrectStreamPartition() {
    // given
    int totalPartition = 5;
    Long sectionId = 1L;
    Long memberId = 1L;
    UUID requestId = UUID.randomUUID();
    List<Long> showSeatIds = List.of(1L, 2L);
    BookingMessage request =
        new BookingMessage(
            BookingMessageType.BOOKING, requestId.toString(), memberId, showSeatIds, sectionId);
    when(messagingProperties.getPartitionCount()).thenReturn(totalPartition);

    StreamOperations<String, String, Object> streamOperations =
        Mockito.mock(StreamOperations.class);
    when(redisTemplate.opsForStream(any(Jackson2HashMapper.class))).thenReturn(streamOperations);

    // when
    bookingMessageProducer.sendMessage(request);

    // then
    verify(streamOperations).add(any(ObjectRecord.class), any(XAddOptions.class));
  }
}
