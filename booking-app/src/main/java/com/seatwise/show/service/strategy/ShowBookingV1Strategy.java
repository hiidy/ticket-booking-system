package com.seatwise.show.service.strategy;

import com.seatwise.show.dto.request.ShowBookingRequest;
import com.seatwise.show.service.ShowBookingService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@ShowBookingStrategyVersion("v1")
@RequiredArgsConstructor
public class ShowBookingV1Strategy implements ShowBookingStrategy {

  private final ShowBookingService showBookingService;

  @Override
  public String createBooking(UUID idempotencyKey, ShowBookingRequest request) {
    return showBookingService.createWithLock(
        idempotencyKey, request.memberId(), request.ticketIds());
  }
}
