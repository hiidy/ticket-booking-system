package com.seatwise.showtime.dto.response;

import com.seatwise.venue.domain.SeatGrade;

public record SeatAvailabilityResponse(SeatGrade grade, Long totalSeats, Long availableSeats) {}
