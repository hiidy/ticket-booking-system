package com.seatwise.event.service;

import com.seatwise.common.exception.ErrorCode;
import com.seatwise.common.exception.NotFoundException;
import com.seatwise.event.domain.Event;
import com.seatwise.event.dto.request.EventRequest;
import com.seatwise.event.dto.response.EventCreateResponse;
import com.seatwise.event.dto.response.EventResponse;
import com.seatwise.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {

  private final EventRepository eventRepository;

  public EventCreateResponse createEvent(EventRequest eventRequest) {
    Event event = eventRepository.save(eventRequest.toEvent());
    return EventCreateResponse.from(event);
  }

  public EventResponse findEventById(Long eventId) {
    return eventRepository
        .findById(eventId)
        .map(EventResponse::from)
        .orElseThrow(() -> new NotFoundException(ErrorCode.EVENT_NOT_FOUND));
  }
}
