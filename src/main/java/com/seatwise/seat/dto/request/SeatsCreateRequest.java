package com.seatwise.seat.dto.request;

import com.seatwise.seat.dto.SeatCreateDto;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SeatsCreateRequest(
    @NotNull Long venueId, @NotNull List<SeatTypeRangeRequest> seatTypeRanges) {

  public SeatCreateDto toCreateDto() {
    return new SeatCreateDto(
        venueId, seatTypeRanges.stream().map(SeatTypeRangeRequest::toSeatTypeRange).toList());
  }
}
