package com.seatwise.booking;

import com.seatwise.booking.dto.BookingRequest;
import com.seatwise.booking.dto.BookingResult;
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

  private final BookingService bookingService;
  private final BookingResultDispatcher waitService;

  @PostMapping
  public DeferredResult<BookingResult> createBookingRequest(
      @RequestHeader("Idempotency-Key") UUID key, @Valid @RequestBody BookingRequest request) {
    DeferredResult<BookingResult> result = waitService.waitForResult(key);
    bookingService.enqueueBooking(key, request);
    return result;
  }
}
