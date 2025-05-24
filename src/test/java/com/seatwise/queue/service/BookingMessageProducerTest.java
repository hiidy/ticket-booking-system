package com.seatwise.queue.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.seatwise.queue.QueueProperties;
import com.seatwise.queue.dto.BookingMessage;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.core.StreamOperations;

@ExtendWith(MockitoExtension.class)
class BookingMessageProducerTest {

  @Mock private StreamOperations<String, String, Object> streamOperations;
  @Mock private QueueProperties queueProperties;
  @InjectMocks private BookingMessageProducer bookingMessageProducer;

  @Test
  void sendMessage() {
    // given
    int totalShard = 5;
    Long sectionId = 1L;
    Long memberId = 1L;
    UUID requestId = UUID.randomUUID();
    List<Long> showSeatIds = List.of(1L, 2L);
    BookingMessage request =
        new BookingMessage(requestId.toString(), memberId, showSeatIds, sectionId);
    when(queueProperties.getShardCount()).thenReturn(totalShard);

    // when
    bookingMessageProducer.sendMessage(request);

    // then
    verify(streamOperations).add(any(ObjectRecord.class), any(XAddOptions.class));
  }
}
