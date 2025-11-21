package com.seatwise.show.dto.response;

public record SeatAvailabilityResponse(String rowName, Long totalSeats, Long availableSeats) {}
