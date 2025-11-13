package com.seatwise.booking.dto;

import java.util.List;

public record BookingMessage(
    BookingMessageType type,
    String requestId,
    Long memberId,
    List<Long> ticketIds,
    Long sectionId) {}
