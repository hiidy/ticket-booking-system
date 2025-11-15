package com.seatwise.booking.strategy;

import com.seatwise.booking.SyncBookingService;
import com.seatwise.booking.dto.request.BookingRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@BookingStrategyVersion("v2")
@RequiredArgsConstructor
public class BookingV2Strategy implements BookingStrategy {

  private final SyncBookingService syncBookingService;

  @Override
  public String createBooking(UUID idempotencyKey, BookingRequest request) {
    return syncBookingService.createWithRedisLock(
        idempotencyKey, request.memberId(), request.ticketIds());
  }
}
