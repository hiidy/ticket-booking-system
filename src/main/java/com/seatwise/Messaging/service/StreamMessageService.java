package com.seatwise.Messaging.service;

import com.seatwise.Messaging.dto.request.StreamMessageRequest;
import com.seatwise.Messaging.entity.StreamMessage;
import com.seatwise.Messaging.repository.StreamMessageRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Getter
public class StreamMessageService {

  private final StreamMessageRepository streamMessageRepository;

  public void saveFailedStreamMessage(StreamMessageRequest request) {
    StreamMessage streamMessage = StreamMessage.failed(request.streamName(), request.messageId());
    streamMessageRepository.save(streamMessage);
  }
}
