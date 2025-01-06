package com.seatwise.booking.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record BookingRequest(@NotNull Long showId, @NotNull List<Long> seatIds) {}
