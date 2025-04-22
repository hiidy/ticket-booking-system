package com.seatwise.queue.service;

import com.seatwise.queue.QueueProperties;
import com.seatwise.queue.StreamKeyGenerator;
import com.seatwise.queue.dto.BookingMessage;
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

  public String sendMessage(BookingMessage message) {
    String streamKey =
        StreamKeyGenerator.forSectionShard(message.sectionId(), queueProperties.getShardCount());

    ObjectRecord<String, BookingMessage> objectRecord =
        StreamRecords.newRecord().in(streamKey).ofObject(message);
    streamOperations.add(objectRecord);
    return message.requestId();
  }
}
