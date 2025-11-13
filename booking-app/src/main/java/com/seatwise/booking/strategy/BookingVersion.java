package com.seatwise.booking.strategy;

public enum BookingVersion {
  V1("v1", "DB Lock 방식"),
  V2("v2", "Redis Lock 방식"),
  V3("v3", "락 없는 방식");

  private final String version;
  private final String description;

  BookingVersion(String version, String description) {
    this.version = version;
    this.description = description;
  }

  public String getVersion() {
    return version;
  }

  public String getDescription() {
    return description;
  }
}
