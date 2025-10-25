package com.seatwise.venue.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VenueCreateRequest(@NotBlank String name, @NotNull Integer totalSeats) {}
