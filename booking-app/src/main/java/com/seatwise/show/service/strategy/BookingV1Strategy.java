package com.seatwise.show.service.strategy;

import com.seatwise.show.service.SyncBookingService;
import com.seatwise.booking.dto.request.BookingRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@BookingStrategyVersion("v1")
@RequiredArgsConstructor
public class BookingV1Strategy implements BookingStrategy {

  private final SyncBookingService syncBookingService;

  @Override
  public String createBooking(UUID idempotencyKey, BookingRequest request) {
    return syncBookingService.createBookingWithDbLock(
        idempotencyKey, request.memberId(), request.ticketIds());
  }
}
