package com.seatwise.show.domain;

public enum Status {
  AVAILABLE("예약 가능"),
  RESERVED("예약 완료"),
  CANCELLED("예약 취소");

  public final String description;

  Status(String description) {
    this.description = description;
  }
}
