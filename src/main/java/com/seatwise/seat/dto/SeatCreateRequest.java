package com.seatwise.seat.dto;

import com.seatwise.seat.domain.Seat;
import com.seatwise.seat.domain.SeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SeatCreateRequest(
    @NotNull Long showId, @NotNull int seatNumber, @NotBlank String seatType) {

  public Seat toEntity() {
    return Seat.builder().seatNumber(seatNumber).type(SeatType.valueOf(seatType)).build();
  }
}
