package com.seatwise.event.dto.response;

import com.seatwise.event.entity.Event;

public record EventCreateResponse(Long id) {

  public static EventCreateResponse from(Event event) {
    return new EventCreateResponse(event.getId());
  }
}
