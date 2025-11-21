package com.seatwise.venue.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SeatRangeRequest(
    @NotBlank String rowName, @NotNull Integer startCol, @NotNull Integer endCol) {}
