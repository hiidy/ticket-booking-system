package com.seatwise.booking.dto.request;

import jakarta.validation.constraints.NotNull;

public record BookingTimeoutRequest(@NotNull Long memberId, @NotNull Long sectionId) {}
