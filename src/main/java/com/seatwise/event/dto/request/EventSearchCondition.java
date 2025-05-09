package com.seatwise.event.dto.request;

import com.seatwise.event.domain.EventType;

public record EventSearchCondition(EventType type, int page, int size) {}
