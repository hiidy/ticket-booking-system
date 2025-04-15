package com.seatwise.queue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.core.StreamOperations;

@ExtendWith(MockitoExtension.class)
class ProduceServiceTest {

  @Mock private StreamOperations<String, String, Object> streamOperations;
  @Mock private QueueProperties queueProperties;
  @InjectMocks private ProduceService produceService;

  @Test
  void sendMessage() {
    // given
    int totalShard = 5;
    Long sectionId = 1L;
    Long memberId = 1L;
    List<Long> showSeatIds = List.of(1L, 2L);
    ProduceRequest request = new ProduceRequest(memberId, showSeatIds, sectionId);
    when(queueProperties.getShardCount()).thenReturn(totalShard);

    // when
    produceService.sendMessage(request);

    // then
    verify(streamOperations).add(any(ObjectRecord.class));
  }
}
