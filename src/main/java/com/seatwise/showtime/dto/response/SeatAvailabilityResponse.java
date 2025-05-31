package com.seatwise.showtime.dto.response;

import com.seatwise.seat.entity.SeatGrade;

public record SeatAvailabilityResponse(SeatGrade grade, int totalSeats, int availableSeats) {}
