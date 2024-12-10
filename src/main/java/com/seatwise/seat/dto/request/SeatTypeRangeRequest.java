package com.seatwise.seat.dto.request;

import com.seatwise.seat.domain.SeatType;
import com.seatwise.seat.dto.SeatTypeRange;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SeatTypeRangeRequest(
    @NotNull Integer startNumber, @NotNull Integer endNumber, @NotBlank String seatType) {

  public SeatTypeRange toSeatTypeRange() {
    return new SeatTypeRange(startNumber, endNumber, SeatType.valueOf(seatType));
  }
}
