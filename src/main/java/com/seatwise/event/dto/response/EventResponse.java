package com.seatwise.event.dto.response;

import com.seatwise.event.domain.Event;

public record EventResponse(Long id, String title, String description, String type) {

  public static EventResponse from(Event event) {
    return new EventResponse(
        event.getId(), event.getTitle(), event.getDescription(), event.getType().name());
  }
}
