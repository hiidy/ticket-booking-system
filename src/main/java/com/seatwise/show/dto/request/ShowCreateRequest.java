package com.seatwise.show.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

public record ShowCreateRequest(
    Long eventId, Long venueId, LocalDate date, LocalTime startTime, LocalTime endTime) {}
