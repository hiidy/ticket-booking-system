package com.seatwise.showtime.dto.request;

import com.seatwise.show.domain.ShowType;
import java.time.LocalDate;

public record ShowSearchCondition(ShowType type, LocalDate date) {}
