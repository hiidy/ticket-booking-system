package com.seatwise.event.controller;

import com.seatwise.event.dto.request.EventRequest;
import com.seatwise.event.dto.request.EventSearchCondition;
import com.seatwise.event.dto.response.EventCreateResponse;
import com.seatwise.event.dto.response.EventResponse;
import com.seatwise.event.service.EventService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

  private final EventService eventService;

  @PostMapping
  public ResponseEntity<Void> createEvent(@Valid @RequestBody EventRequest eventRequest) {
    EventCreateResponse response = eventService.createEvent(eventRequest);
    return ResponseEntity.created(URI.create("/api/events/" + response.id())).build();
  }

  @GetMapping("/{eventId}")
  public ResponseEntity<EventResponse> findEventById(@PathVariable Long eventId) {
    return ResponseEntity.ok(eventService.findEventById(eventId));
  }

  @GetMapping
  public ResponseEntity<List<EventResponse>> getEvents(
      @ModelAttribute EventSearchCondition condition) {
    return ResponseEntity.ok(eventService.getEvents(condition));
  }
}
