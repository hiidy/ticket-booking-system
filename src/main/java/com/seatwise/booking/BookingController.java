package com.seatwise.booking;

import com.seatwise.booking.dto.BookingMessage;
import com.seatwise.booking.dto.request.BookingRequest;
import com.seatwise.booking.dto.response.BookingResponse;
import com.seatwise.booking.messaging.BookingMessageProducer;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

  private final BookingMessageProducer producer;
  private final BookingResponseManager responseManager;

  @PostMapping
  public DeferredResult<BookingResponse> createBookingRequest(
      @RequestHeader("Idempotency-Key") UUID key, @Valid @RequestBody BookingRequest request) {
    DeferredResult<BookingResponse> response = responseManager.createPendingResponse(key);
    producer.enqueueBooking(
        new BookingMessage(
            key.toString(), request.memberId(), request.ticketIds(), request.sectionId()));
    return response;
  }
}
