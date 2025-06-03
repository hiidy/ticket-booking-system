package com.seatwise.ticket;

import com.seatwise.showtime.dto.response.SeatAvailabilityResponse;
import com.seatwise.ticket.dto.TicketCreateRequest;
import com.seatwise.ticket.dto.TicketResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

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

  @GetMapping("/{showTimeId}/seat-availability")
  public ResponseEntity<List<SeatAvailabilityResponse>> getSeatAvailability(
      @PathVariable Long showTimeId) {
    List<SeatAvailabilityResponse> response = ticketService.getAvailableSeatsForShow(showTimeId);
    return ResponseEntity.ok(response);
  }
}
