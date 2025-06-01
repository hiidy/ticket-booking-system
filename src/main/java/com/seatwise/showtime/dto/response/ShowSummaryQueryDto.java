package com.seatwise.showtime.dto.response;

import com.seatwise.show.domain.ShowType;
import java.time.LocalDate;
import java.time.LocalTime;

public record ShowSummaryQueryDto(
    Long showId,
    String title,
    ShowType type,
    LocalDate date,
    LocalTime startTime,
    String venueName) {}
