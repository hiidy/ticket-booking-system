package com.seatwise.event.dto.response;

import com.seatwise.event.entity.Event;
import com.seatwise.event.entity.EventType;

public record EventResponse(Long id, String title, String description, EventType type) {

  public static EventResponse from(Event event) {
    return new EventResponse(
        event.getId(), event.getTitle(), event.getDescription(), event.getType());
  }
}
