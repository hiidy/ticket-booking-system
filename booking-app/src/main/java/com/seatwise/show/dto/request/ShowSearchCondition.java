package com.seatwise.show.dto.request;

import com.seatwise.show.entity.ShowType;
import java.time.LocalDate;

public record ShowSearchCondition(ShowType type, LocalDate startDate, LocalDate endDate) {}
