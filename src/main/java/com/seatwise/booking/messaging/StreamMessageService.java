package com.seatwise.booking.messaging;

import com.seatwise.booking.domain.BookingStreamMessage;
import com.seatwise.booking.domain.BookingStreamMessageRepository;
import com.seatwise.booking.dto.request.StreamMessageRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Getter
public class StreamMessageService {

  private final BookingStreamMessageRepository bookingStreamMessageRepository;

  public void saveFailedStreamMessage(StreamMessageRequest request) {
    BookingStreamMessage bookingStreamMessage =
        BookingStreamMessage.failed(request.streamName(), request.messageId());
    bookingStreamMessageRepository.save(bookingStreamMessage);
  }
}
