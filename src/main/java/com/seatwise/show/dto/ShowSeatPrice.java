package com.seatwise.show.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ShowSeatPrice(
    @NotNull Long startSeatId, @NotNull Long endSeatId, @Positive Integer price) {}
