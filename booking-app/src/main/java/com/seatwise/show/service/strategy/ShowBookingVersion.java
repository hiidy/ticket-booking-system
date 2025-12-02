package com.seatwise.show.service.strategy;

import lombok.Getter;

@Getter
public enum ShowBookingVersion {
  V1("v1", "DB Lock 방식"),
  V2("v2", "Redisson Multi Lock 방식"),
  V21("v21", "faster multi lock"),
  V3("v3", "Faster Multi lock with Async Booking");

  private final String version;
  private final String description;

  ShowBookingVersion(String version, String description) {
    this.version = version;
    this.description = description;
  }
}
