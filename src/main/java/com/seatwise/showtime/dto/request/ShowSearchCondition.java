package com.seatwise.showtime.dto.request;

import com.seatwise.show.ShowType;
import java.time.LocalDate;

public record ShowSearchCondition(ShowType type, LocalDate startDate, LocalDate endDate) {}
