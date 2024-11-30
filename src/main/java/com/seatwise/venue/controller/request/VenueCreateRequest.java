package com.seatwise.venue.controller.request;

import com.seatwise.venue.service.dto.VenueCreateDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VenueCreateRequest(@NotBlank String name, @NotNull Integer totalSeats) {

  public VenueCreateDto toCreateDto() {
    return new VenueCreateDto(name, totalSeats);
  }
}
