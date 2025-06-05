package com.seatwise.venue.dto.response;

import com.seatwise.venue.domain.Seat;
import java.util.List;

public record SeatCreateResponse(List<Long> seatsId) {

  public static SeatCreateResponse from(List<Seat> seats) {
    List<Long> seatsId = seats.stream().map(Seat::getId).toList();
    return new SeatCreateResponse(seatsId);
  }
}
