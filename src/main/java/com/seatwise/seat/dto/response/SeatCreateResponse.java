package com.seatwise.seat.dto.response;

import com.seatwise.seat.domain.Seat;

public record SeatCreateResponse(Long seatId) {

  public static SeatCreateResponse from(Seat seat) {
    return new SeatCreateResponse(seat.getId());
  }
}
