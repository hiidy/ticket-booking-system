package com.seatwise.seat.dto;

public record SeatsCreateRequest(Long showId, int maxSeatNumber, String seatType) {}
