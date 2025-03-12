package com.seatwise.show.dto.response;

import com.seatwise.seat.domain.SeatGrade;
import com.seatwise.show.domain.ShowSeat;
import java.time.LocalDateTime;

public record ShowSeatResponse(
    Long showSeatId, int seatNumber, String status, SeatGrade seatGrade, boolean isLocked) {

  public static ShowSeatResponse from(ShowSeat showSeat, LocalDateTime currentTime) {
    return new ShowSeatResponse(
        showSeat.getId(),
        showSeat.getSeat().getSeatNumber(),
        showSeat.getStatus().getDescription(),
        showSeat.getSeat().getGrade(),
        showSeat.isLocked(currentTime));
  }
}
