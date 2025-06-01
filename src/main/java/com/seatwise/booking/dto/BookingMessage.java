package com.seatwise.booking.dto;

import java.util.List;

public record BookingMessage(
    String requestId, Long memberId, List<Long> showSeatIds, Long sectionId) {}
