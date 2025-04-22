package com.seatwise.booking.controller;

import com.seatwise.booking.dto.BookingRequest;
import com.seatwise.booking.dto.BookingResult;
import com.seatwise.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

  private final BookingService bookingService;

  @PostMapping
  public ResponseEntity<String> createBookingRequest(@Valid @RequestBody BookingRequest request) {
    String requestId = bookingService.enqueueBooking(request);
    return ResponseEntity.ok(requestId);
  }

  @GetMapping("/{requestId}")
  public ResponseEntity<BookingResult> getBookingResult(@PathVariable String requestId) {
    BookingResult response = bookingService.readBookingResult(requestId);
    return ResponseEntity.ok(response);
  }
}
