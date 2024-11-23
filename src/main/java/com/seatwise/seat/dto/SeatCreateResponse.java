package com.seatwise.seat.dto;

import com.seatwise.seat.entity.Seat;

public record SeatCreateResponse(Long seatId) {

  public static SeatCreateResponse from(Seat seat) {
    return new SeatCreateResponse(seat.getId());
  }
}
