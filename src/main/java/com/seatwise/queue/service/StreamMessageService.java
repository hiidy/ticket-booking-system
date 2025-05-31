package com.seatwise.queue.service;

import com.seatwise.queue.dto.request.StreamMessageRequest;
import com.seatwise.queue.entity.StreamMessage;
import com.seatwise.queue.repository.StreamMessageRepository;
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
