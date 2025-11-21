package com.seatwise.show.dto.response;

import com.seatwise.show.entity.Ticket;
import java.time.LocalDateTime;

public record TicketResponse(Long ticketId, String seatNumber, String status, boolean isLocked) {

  public static TicketResponse from(Ticket ticket, LocalDateTime currentTime) {
    return new TicketResponse(
        ticket.getId(),
        ticket.getSeat().getRowName() + "-" + ticket.getSeat().getColName(),
        ticket.getStatus().getDescription(),
        ticket.canAssignBooking(currentTime));
  }
}
