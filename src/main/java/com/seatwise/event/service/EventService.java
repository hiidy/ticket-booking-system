package com.seatwise.event.service;

import com.seatwise.common.exception.BusinessException;
import com.seatwise.common.exception.ErrorCode;
import com.seatwise.event.dto.request.EventRequest;
import com.seatwise.event.dto.response.EventCreateResponse;
import com.seatwise.event.dto.response.EventResponse;
import com.seatwise.event.entity.Event;
import com.seatwise.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
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
        .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
  }
}
