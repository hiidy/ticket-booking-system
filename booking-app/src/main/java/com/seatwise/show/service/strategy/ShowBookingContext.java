package com.seatwise.show.service.strategy;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ShowBookingContext {

  private final Map<String, ShowBookingStrategy> strategies;

  public ShowBookingContext(List<ShowBookingStrategy> allStrategies) {
    this.strategies =
        allStrategies.stream()
            .collect(
                Collectors.toMap(
                    strategy ->
                        strategy.getClass().getAnnotation(ShowBookingStrategyVersion.class).value(),
                    Function.identity()));
  }

  public ShowBookingStrategy get(String version) {
    ShowBookingStrategy strategy = strategies.get(version);
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
