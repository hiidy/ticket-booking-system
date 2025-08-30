package com.seatwise.booking.dto;

import java.util.List;

public record BookingCreatedEvent(List<Long> ticketIds, Long memberId) {}
