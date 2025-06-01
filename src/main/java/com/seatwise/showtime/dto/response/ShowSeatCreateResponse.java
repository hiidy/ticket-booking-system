package com.seatwise.showtime.dto.response;

import com.seatwise.showtime.domain.ShowSeat;
import java.util.List;

public record ShowSeatCreateResponse(List<Long> showSeatIds) {

  public static ShowSeatCreateResponse from(List<ShowSeat> showSeats) {
    return new ShowSeatCreateResponse(showSeats.stream().map(ShowSeat::getId).toList());
  }
}
