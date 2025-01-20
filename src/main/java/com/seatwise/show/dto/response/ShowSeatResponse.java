package com.seatwise.show.dto.response;

import com.seatwise.show.domain.ShowSeat;

public record ShowSeatResponse(Long showSeatId, int seatNumber, String status) {

  public static ShowSeatResponse from(ShowSeat showSeat) {
    return new ShowSeatResponse(
        showSeat.getId(),
        showSeat.getSeat().getSeatNumber(),
        showSeat.getStatus().getDescription());
  }
}
