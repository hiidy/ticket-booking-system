package com.seatwise.redis;

import lombok.Getter;

@Getter
public enum RedisKeys {
  TICKET_AVAILABLE("ticket_available_%s_%s"),
  TICKET_LOCKED("ticket_locked_%s_%s"),
  TICKET_BOOKED("ticket_booked_%s_%s"),
  CACHE_SHOW_SECTION_NULL("cache:show_section_null:%s:%s");

  private final String key;

  RedisKeys(String key) {
    this.key = key;
  }
}
