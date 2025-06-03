package com.seatwise.booking.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record BookingRequest(
    @NotNull Long memberId, @NotNull @NotEmpty List<Long> ticketIds, @NotNull Long sectionId) {}
