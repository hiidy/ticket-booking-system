package com.seatwise.event.domain;

public enum EventType {
  CONCERT("콘서트"),
  MUSICAL("뮤지컬"),
  THEATER("연극");

  private final String description;

  EventType(String description) {
    this.description = description;
  }
}
