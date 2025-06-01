package com.seatwise.showtime.dto.response;

import com.seatwise.seat.domain.SeatGrade;

public record SeatAvailabilityResponse(SeatGrade grade, int totalSeats, int availableSeats) {}
