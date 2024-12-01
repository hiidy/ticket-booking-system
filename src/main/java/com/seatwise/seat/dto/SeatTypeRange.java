package com.seatwise.seat.dto;

import com.seatwise.seat.domain.SeatType;

public record SeatTypeRange(Integer startNumber, Integer endNumber, SeatType seatType) {}
