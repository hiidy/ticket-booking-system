package com.seatwise.booking.messaging;

import com.seatwise.booking.BookingResultDispatcher;
import com.seatwise.booking.BookingService;
import com.seatwise.booking.dto.BookingMessage;
import com.seatwise.booking.dto.BookingResult;
import com.seatwise.booking.exception.BookingException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingMessageHandler {

  private final BookingService bookingService;
  private final BookingResultDispatcher dispatcher;

  public void handleBookingMessage(BookingMessage request) {
    UUID requestId = UUID.fromString(request.requestId());
    log.info(
        "멤버Id: {}, 좌석Id: {}, 섹션Id: {}에 대한 요청 처리중",
        request.memberId(),
        request.showSeatIds(),
        request.sectionId());
    try {
      Long bookingId =
          bookingService.createBooking(requestId, request.memberId(), request.showSeatIds());
      BookingResult result = BookingResult.success(bookingId, requestId);
      dispatcher.completeResult(requestId, result);
    } catch (BookingException e) {
      dispatcher.completeWithFailure(requestId, e);
    }
  }
}
