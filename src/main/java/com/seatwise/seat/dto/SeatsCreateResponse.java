package com.seatwise.seat.dto;

import com.seatwise.seat.entity.Seat;
import java.util.List;

public record SeatsCreateResponse(List<Long> seatsId) {

  public static SeatsCreateResponse from(List<Seat> seats) {
    List<Long> seatsId = seats.stream().map(Seat::getId).toList();
    return new SeatsCreateResponse(seatsId);
  }
}
