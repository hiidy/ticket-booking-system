package com.seatwise.event.service;

import com.seatwise.common.exception.BusinessException;
import com.seatwise.common.exception.ErrorCode;
import com.seatwise.event.domain.Event;
import com.seatwise.event.dto.request.EventRequest;
import com.seatwise.event.dto.request.EventSearchCondition;
import com.seatwise.event.dto.response.EventCreateResponse;
import com.seatwise.event.dto.response.EventResponse;
import com.seatwise.event.repository.EventRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

  public List<EventResponse> getEvents(EventSearchCondition condition) {
    Pageable pageable = PageRequest.of(condition.page(), condition.size());
    List<Event> events = eventRepository.findAllByType(condition.type(), pageable);
    return events.stream().map(EventResponse::from).toList();
  }
}
