package com.seatwise.show.dto.response;

import com.seatwise.seat.domain.SeatGrade;

public record SeatAvailabilityResponse(SeatGrade grade, int totalSeats, int availableSeats) {}
