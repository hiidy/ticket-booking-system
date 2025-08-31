package com.seatwise.ticket;

import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketCacheService {

  private final RedisTemplate<String, String> redisTemplate;
  private static final String TICKET_KEY = "ticket:";

  public void holdTickets(List<Long> ticketIds, Long memberId) {
    for (Long ticketId : ticketIds) {
      redisTemplate
          .opsForValue()
          .set(TICKET_KEY + ticketId, memberId.toString(), 10, TimeUnit.MINUTES);
    }
  }

  public boolean hasUnavailableTickets(List<Long> ticketIds, Long memberId) {
    List<String> keys = ticketIds.stream().map(ticketId -> TICKET_KEY + ticketId).toList();
    List<String> values = redisTemplate.opsForValue().multiGet(keys);
    return values != null
        && values.stream().anyMatch(value -> value != null && !value.equals(memberId.toString()));
  }
}
