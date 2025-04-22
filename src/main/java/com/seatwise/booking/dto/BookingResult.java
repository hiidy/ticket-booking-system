package com.seatwise.booking.dto;

public record BookingResult(boolean success, Long bookingId, String requestId) {}
