package com.seatwise.seat.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SeatCreateRequest(
    @NotNull Long venueId, @NotNull List<SeatGradeRangeRequest> seatTypeRanges) {}
