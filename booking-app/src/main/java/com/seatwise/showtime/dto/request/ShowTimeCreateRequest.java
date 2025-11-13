package com.seatwise.showtime.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

public record ShowTimeCreateRequest(
    Long showId, Long venueId, LocalDate date, LocalTime startTime, LocalTime endTime) {}
