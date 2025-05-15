package com.seatwise.show.dto.response;

import com.seatwise.event.domain.EventType;
import java.time.LocalDate;
import java.time.LocalTime;

public record ShowSummaryQueryDto(
    Long showId,
    String eventTitle,
    EventType eventType,
    LocalDate date,
    LocalTime startTime,
    String venueName) {}
