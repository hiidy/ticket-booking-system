package com.seatwise.booking.controller;

import com.seatwise.booking.dto.BookingRequest;
import com.seatwise.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<Long> createBooking(@Valid @RequestBody BookingRequest bookingRequest) {
    Long bookingId =
        bookingService.createBooking(bookingRequest.showId(), bookingRequest.seatIds());
    return ResponseEntity.ok(bookingId);
  }
}
