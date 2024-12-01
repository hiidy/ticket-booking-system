package com.seatwise.seat.dto;

import java.util.List;

public record SeatCreateDto(Long venueId, List<SeatTypeRange> seatTypeRanges) {}
