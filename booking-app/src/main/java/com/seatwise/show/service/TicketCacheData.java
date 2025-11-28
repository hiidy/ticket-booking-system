package com.seatwise.show.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seatwise.redis.RedisCache;
import com.seatwise.show.dto.TicketAvailability;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TicketCacheData {

  private final RedisCache redisCache;
  private final ObjectMapper objectMapper;

  public List<TicketAvailability> getData(String key) {
    List<Object> values = redisCache.hashValues(key);

    if (values == null || values.isEmpty()) {
      return new ArrayList<>();
    }

    return values.stream().map(Object::toString).map(this::parseJsonToTicketAvailability).toList();
  }

  private TicketAvailability parseJsonToTicketAvailability(String json) {
    try {
      return objectMapper.readValue(json, TicketAvailability.class);
    } catch (Exception e) {
      throw new RuntimeException("JSON 파싱 실패: " + json, e);
    }
  }
}
