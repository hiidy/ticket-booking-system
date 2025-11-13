package com.seatwise.booking.strategy;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingContext {

  private final Map<String, BookingStrategy> strategies = new HashMap<>();

    public BookingStrategy get(String version) {
        BookingStrategy strategy = strategies.get(version);
        if (strategy == null) {
            throw new IllegalArgumentException("지원하지 않는 버전입니다: " + version);
        }
        return strategy;
    }
}