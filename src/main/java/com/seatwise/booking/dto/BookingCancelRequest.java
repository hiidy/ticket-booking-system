package com.seatwise.booking.dto;

import jakarta.validation.constraints.NotNull;

public record BookingCancelRequest(@NotNull Long memberId, @NotNull Long bookingId) {}
