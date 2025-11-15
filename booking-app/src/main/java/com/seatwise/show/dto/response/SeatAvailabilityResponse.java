package com.seatwise.show.dto.response;

import com.seatwise.venue.entity.SeatGrade;

public record SeatAvailabilityResponse(SeatGrade grade, Long totalSeats, Long availableSeats) {}
