package com.seatwise.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TicketPrice(
    @NotNull Long startSeatId, @NotNull Long endSeatId, @Positive Integer price) {}
