package com.seatwise.show.service.strategy;

import com.seatwise.booking.dto.request.BookingRequest;
import com.seatwise.show.service.ShowBookingService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@BookingStrategyVersion("v1")
@RequiredArgsConstructor
public class BookingV1Strategy implements BookingStrategy {

  private final ShowBookingService showBookingService;

  @Override
  public String createBooking(UUID idempotencyKey, BookingRequest request) {
    return showBookingService.createWithLock(
        idempotencyKey, request.memberId(), request.ticketIds());
  }
}
