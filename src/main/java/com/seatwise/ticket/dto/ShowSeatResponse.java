package com.seatwise.ticket.dto;

import com.seatwise.seat.domain.SeatGrade;
import com.seatwise.ticket.domain.Ticket;
import java.time.LocalDateTime;

public record ShowSeatResponse(
    Long showSeatId, int seatNumber, String status, SeatGrade seatGrade, boolean isLocked) {

  public static ShowSeatResponse from(Ticket ticket, LocalDateTime currentTime) {
    return new ShowSeatResponse(
        ticket.getId(),
        ticket.getSeat().getSeatNumber(),
        ticket.getStatus().getDescription(),
        ticket.getSeat().getGrade(),
        ticket.canAssignBooking(currentTime));
  }
}
