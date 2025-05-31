package com.seatwise.event.dto.request;

import com.seatwise.event.entity.Event;
import com.seatwise.event.entity.EventType;
import jakarta.validation.constraints.NotBlank;

public record EventRequest(
    @NotBlank String title, @NotBlank String description, @NotBlank String eventType) {

  public Event toEvent() {
    return Event.builder()
        .title(title)
        .description(description)
        .type(EventType.valueOf(eventType))
        .build();
  }
}
