package com.seatwise.common.builder;

import com.seatwise.event.domain.Event;
import com.seatwise.event.domain.EventType;
import com.seatwise.event.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventTestDataBuilder {

  @Autowired private EventRepository eventRepository;
  private String title;
  private String description;
  private EventType type;

  public EventTestDataBuilder(EventRepository eventRepository) {
    this.eventRepository = eventRepository;
  }

  public EventTestDataBuilder withTitle(String title) {
    this.title = title;
    return this;
  }

  public EventTestDataBuilder withDescription(String description) {
    this.description = description;
    return this;
  }

  public EventTestDataBuilder withType(EventType type) {
    this.type = type;
    return this;
  }

  public Event build() {
    Event event = new Event(title, description, type);
    return eventRepository.save(event);
  }
}
