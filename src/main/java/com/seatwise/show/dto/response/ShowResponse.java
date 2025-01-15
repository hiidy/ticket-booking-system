package com.seatwise.show.dto.response;

import java.time.LocalTime;
import java.util.List;

public record ShowResponse(LocalTime startTime, List<SeatRemainingResponse> remainingSeats) {}
