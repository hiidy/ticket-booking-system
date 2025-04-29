package com.seatwise.booking.controller;

import com.seatwise.booking.dto.BookingRequest;
import com.seatwise.booking.dto.BookingResult;
import com.seatwise.booking.service.BookingResultWaitService;
import com.seatwise.booking.service.BookingService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

  private final BookingService bookingService;
  private final BookingResultWaitService waitService;

  @PostMapping
  public DeferredResult<BookingResult> createBookingRequest(
      @Valid @RequestBody BookingRequest request) {
    String requestId = UUID.randomUUID().toString();
    DeferredResult<BookingResult> result = waitService.waitForResult(requestId);
    bookingService.enqueueBooking(requestId, request);
    return result;
  }

  @GetMapping("/{requestId}")
  public ResponseEntity<BookingResult> getBookingResult(@PathVariable String requestId) {
    BookingResult response = bookingService.readBookingResult(requestId);
    return ResponseEntity.ok(response);
  }
}
