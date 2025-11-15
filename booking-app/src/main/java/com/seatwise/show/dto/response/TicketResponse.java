package com.seatwise.show.dto.response;

import com.seatwise.show.entity.Ticket;
import com.seatwise.venue.entity.SeatGrade;
import java.time.LocalDateTime;

public record TicketResponse(
    Long ticketId, int seatNumber, String status, SeatGrade seatGrade, boolean isLocked) {

  public static TicketResponse from(Ticket ticket, LocalDateTime currentTime) {
    return new TicketResponse(
        ticket.getId(),
        ticket.getSeat().getSeatNumber(),
        ticket.getStatus().getDescription(),
        ticket.getSeat().getGrade(),
        ticket.canAssignBooking(currentTime));
  }
}
