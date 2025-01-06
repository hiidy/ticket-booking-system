package com.seatwise.seat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SeatTypeRangeRequest(
    @NotNull Integer startNumber, @NotNull Integer endNumber, @NotBlank String seatType) {}
