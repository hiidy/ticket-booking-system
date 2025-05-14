package com.seatwise.show.dto.request;

import com.seatwise.event.domain.EventType;
import java.time.LocalDate;

public record ShowSearchCondition(EventType type, LocalDate date) {}
