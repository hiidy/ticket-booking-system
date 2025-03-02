package com.seatwise.booking.repository;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisBookingRepository {

  private final RedisTemplate<String, String> redisTemplate;
  private static final String LOCK_PREFIX = "lock:showSeat:";
  private static final long LOCK_DURATION = 10;

  public boolean lockSeat(Long memberId, Long seatId) {
    String key = LOCK_PREFIX + seatId;

    Boolean isLocked =
        redisTemplate
            .opsForValue()
            .setIfAbsent(key, memberId.toString(), LOCK_DURATION, TimeUnit.MINUTES);
    return Boolean.TRUE.equals(isLocked);
  }
}
