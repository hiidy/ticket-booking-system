package com.seatwise.queue;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingMessageProducer {

  private final QueueProperties queueProperties;
  private final StreamOperations<String, String, Object> streamOperations;

  public void sendMessage(ProduceRequest request) {
    String streamKey =
        StreamKeyGenerator.forSectionShard(request.sectionId(), queueProperties.getShardCount());

    ObjectRecord<String, ProduceRequest> objectRecord =
        StreamRecords.newRecord().in(streamKey).ofObject(request);
    streamOperations.add(objectRecord);
  }
}
