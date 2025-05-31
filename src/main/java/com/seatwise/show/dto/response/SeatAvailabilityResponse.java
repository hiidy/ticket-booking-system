package com.seatwise.show.dto.response;

import com.seatwise.seat.entity.SeatGrade;

public record SeatAvailabilityResponse(SeatGrade grade, int totalSeats, int availableSeats) {}
