package com.seatwise.booking.entity;

import lombok.Getter;

@Getter
public enum MessageStatus {
  PENDING,
  COMPLETED,
  FAILED,
  CANCELLED
}
