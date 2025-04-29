package com.seatwise.booking.dto;

public record BookingResult(boolean success, Long bookingId, String requestId) {

  public static BookingResult success(Long bookingId, String requestId) {
    return new BookingResult(true, bookingId, requestId);
  }

  public static BookingResult failed(String requestId) {
    return new BookingResult(false, null, requestId);
  }
}
