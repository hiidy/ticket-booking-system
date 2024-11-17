package com.seatwise.show.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

public record ShowCreateRequest(
    Long eventId, LocalDate date, LocalTime startTime, LocalTime endTime) {}
