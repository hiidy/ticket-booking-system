package com.seatwise.booking.controller;

import com.seatwise.booking.dto.BookingRequest;
import com.seatwise.booking.service.BookingFacadeService;
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

  private final BookingFacadeService bookingFacadeService;

  @PostMapping
  public ResponseEntity<Long> createBooking(@Valid @RequestBody BookingRequest bookingRequest) {
    Long bookingId = bookingFacadeService.createBookingFacade(bookingRequest.showSeatIds());
    return ResponseEntity.ok(bookingId);
  }
}
