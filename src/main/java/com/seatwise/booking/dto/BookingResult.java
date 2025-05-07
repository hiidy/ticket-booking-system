package com.seatwise.booking.dto;

import java.util.UUID;

public record BookingResult(boolean success, Long bookingId, UUID requestId) {

  public static BookingResult success(Long bookingId, UUID requestId) {
    return new BookingResult(true, bookingId, requestId);
  }

  public static BookingResult failed(UUID requestId) {
    return new BookingResult(false, null, requestId);
  }
}
