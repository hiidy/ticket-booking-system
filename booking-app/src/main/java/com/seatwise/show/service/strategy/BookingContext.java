package com.seatwise.show.service.strategy;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class BookingContext {

  private final Map<String, BookingStrategy> strategies;

  public BookingContext(List<BookingStrategy> allStrategies) {
    this.strategies =
        allStrategies.stream()
            .collect(
                Collectors.toMap(
                    strategy ->
                        strategy.getClass().getAnnotation(BookingStrategyVersion.class).value(),
                    Function.identity()));
  }

  public BookingStrategy get(String version) {
    BookingStrategy strategy = strategies.get(version);
    if (strategy == null) {
      throw new IllegalArgumentException("지원하지 않는 버전입니다: " + version);
    }
    return strategy;
  }

  public Map<String, String> getSupportedVersions() {
    return strategies.entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().getClass().getSimpleName().replace("Strategy", "")));
  }
}
