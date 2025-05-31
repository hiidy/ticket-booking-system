package com.seatwise.showtime.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

public record ShowTimeCreateRequest(
    Long eventId, Long venueId, LocalDate date, LocalTime startTime, LocalTime endTime) {}
