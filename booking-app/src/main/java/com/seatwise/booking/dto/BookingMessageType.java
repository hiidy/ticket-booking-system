package com.seatwise.booking.dto;

import lombok.Getter;

@Getter
public enum BookingMessageType {
  BOOKING,
  CLIENT_TIMEOUT_CANCEL,
  CANCEL,
}
