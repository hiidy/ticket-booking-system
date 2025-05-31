package com.seatwise.showtime.dto.response;

import java.time.LocalTime;
import java.util.List;

public record ShowTimeResponse(LocalTime startTime, List<SeatRemainingResponse> remainingSeats) {}
