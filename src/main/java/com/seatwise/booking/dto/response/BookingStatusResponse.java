package com.seatwise.booking.dto.response;

import java.util.UUID;

public record BookingStatusResponse(boolean success, Long bookingId, UUID requestId) {

  public static BookingStatusResponse success(Long bookingId, UUID requestId) {
    return new BookingStatusResponse(true, bookingId, requestId);
  }

  public static BookingStatusResponse pending(UUID requestId) {
    return new BookingStatusResponse(false, null, requestId);
  }
}
