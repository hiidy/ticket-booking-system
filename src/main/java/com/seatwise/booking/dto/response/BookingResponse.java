package com.seatwise.booking.dto.response;

import java.util.UUID;

public record BookingResponse(boolean success, Long bookingId, UUID requestId) {

  public static BookingResponse success(Long bookingId, UUID requestId) {
    return new BookingResponse(true, bookingId, requestId);
  }

  public static BookingResponse failed(UUID requestId) {
    return new BookingResponse(false, null, requestId);
  }
}
