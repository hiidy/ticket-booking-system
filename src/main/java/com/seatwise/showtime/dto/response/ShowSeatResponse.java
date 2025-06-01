package com.seatwise.showtime.dto.response;

import com.seatwise.seat.domain.SeatGrade;
import com.seatwise.showtime.domain.ShowSeat;
import java.time.LocalDateTime;

public record ShowSeatResponse(
    Long showSeatId, int seatNumber, String status, SeatGrade seatGrade, boolean isLocked) {

  public static ShowSeatResponse from(ShowSeat showSeat, LocalDateTime currentTime) {
    return new ShowSeatResponse(
        showSeat.getId(),
        showSeat.getSeat().getSeatNumber(),
        showSeat.getStatus().getDescription(),
        showSeat.getSeat().getGrade(),
        showSeat.canAssignBooking(currentTime));
  }
}
