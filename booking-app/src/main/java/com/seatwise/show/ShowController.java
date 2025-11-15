package com.seatwise.show;

import com.seatwise.show.dto.request.ShowRequest;
import com.seatwise.show.dto.response.SeatAvailabilityResponse;
import com.seatwise.show.dto.response.ShowCreateResponse;
import com.seatwise.show.dto.response.ShowResponse;
import com.seatwise.show.service.ShowService;
import com.seatwise.show.service.TicketService;
import com.seatwise.show.dto.request.TicketCreateRequest;
import com.seatwise.show.dto.response.TicketResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class ShowController {

  private final ShowService showService;

  @PostMapping
  public ResponseEntity<Void> createEvent(@Valid @RequestBody ShowRequest showRequest) {
    ShowCreateResponse response = showService.createEvent(showRequest);
    return ResponseEntity.created(URI.create("/api/events/" + response.id())).build();
  }

  @GetMapping("/{eventId}")
  public ResponseEntity<ShowResponse> findEventById(@PathVariable Long eventId) {
    return ResponseEntity.ok(showService.findEventById(eventId));
  }

  @RestController
  @RequestMapping("/api/tickets")
  @RequiredArgsConstructor
  public static class TicketController {

    private final TicketService ticketService;

    @PostMapping("/{showTimeId}/seats")
    public ResponseEntity<List<Long>> createTickets(
        @PathVariable Long showTimeId, @Valid @RequestBody TicketCreateRequest request) {
      List<Long> response = ticketService.createTickets(showTimeId, request);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{showTimeId}/seats")
    public ResponseEntity<List<TicketResponse>> getTickets(@PathVariable Long showTimeId) {
      List<TicketResponse> response = ticketService.getTickets(showTimeId);
      return ResponseEntity.ok(response);
    }

    @GetMapping("/availability")
    public ResponseEntity<List<SeatAvailabilityResponse>> getTicketAvailabilityByGrade(
        @RequestParam Long showTimeId) {
      List<SeatAvailabilityResponse> response =
          ticketService.getTicketAvailabilityByGrade(showTimeId);
      return ResponseEntity.ok(response);
    }
  }
}
