package com.seatwise.venue.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SeatGradeRangeRequest(
    @NotNull Integer startNumber, @NotNull Integer endNumber, @NotBlank String grade) {}
