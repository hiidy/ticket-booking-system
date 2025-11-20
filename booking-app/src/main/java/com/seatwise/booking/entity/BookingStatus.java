package com.seatwise.booking.entity;

import lombok.Getter;

@Getter
public enum BookingStatus {
  PENDING,
  FAILED,
  SUCCESS,
  CANCELLED
}
