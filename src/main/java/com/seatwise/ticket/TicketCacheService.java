package com.seatwise.ticket;

import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketCacheService {

  private final RedisTemplate<String, Object> redisTemplate;
  private static final String TICKET_KEY = "ticket:";

  public void cacheTicketBooking(List<Long> ticketIds, Long memberId) {
    for (Long ticketId : ticketIds) {
      redisTemplate.opsForValue().set(TICKET_KEY + ticketId, memberId, 10, TimeUnit.MINUTES);
    }
  }
}
