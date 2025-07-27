package com.seatwise.booking.dto;

import jakarta.validation.constraints.NotNull;

public record BookingTimeoutRequest(@NotNull Long memberId, @NotNull Long sectionId) {}
