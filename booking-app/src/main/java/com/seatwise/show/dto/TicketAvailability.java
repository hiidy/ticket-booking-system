package com.seatwise.show.dto;

import com.seatwise.show.entity.TicketStatus;

public record TicketAvailability(
    Long ticketId,
    Long showId,
    Long sectionId,
    String rowName,
    String colName,
    TicketStatus status) {}
