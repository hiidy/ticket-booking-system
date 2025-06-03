package com.seatwise.ticket;

import com.seatwise.showtime.dto.response.SeatAvailabilityResponse;
import com.seatwise.ticket.dto.ShowSeatCreateRequest;
import com.seatwise.ticket.dto.ShowSeatResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/showtimes")
@RequiredArgsConstructor
public class TicketController {

  private final TicketService ticketService;

  @PostMapping("/{showTimeId}/seats")
  public ResponseEntity<List<Long>> createShowSeats(
      @PathVariable Long showTimeId, @Valid @RequestBody ShowSeatCreateRequest request) {
    List<Long> response = ticketService.createShowSeat(showTimeId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{showTimeId}/seats")
  public ResponseEntity<List<ShowSeatResponse>> getShowSeats(@PathVariable Long showTimeId) {
    List<ShowSeatResponse> response = ticketService.getShowSeats(showTimeId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{showTimeId}/seat-availability")
  public ResponseEntity<List<SeatAvailabilityResponse>> getSeatAvailability(
      @PathVariable Long showTimeId) {
    List<SeatAvailabilityResponse> response = ticketService.getAvailableSeatsForShow(showTimeId);
    return ResponseEntity.ok(response);
  }
}
