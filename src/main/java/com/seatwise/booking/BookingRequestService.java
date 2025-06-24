package com.seatwise.booking;

import com.seatwise.booking.dto.BookingMessage;
import com.seatwise.booking.dto.BookingRequest;
import com.seatwise.booking.messaging.BookingMessageProducer;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingRequestService {

  private final BookingMessageProducer producer;

  public void enqueueBooking(UUID requestId, BookingRequest request) {
    BookingMessage message =
        new BookingMessage(
            requestId.toString(), request.memberId(), request.ticketIds(), request.sectionId());
    producer.sendMessage(message);
  }
}
