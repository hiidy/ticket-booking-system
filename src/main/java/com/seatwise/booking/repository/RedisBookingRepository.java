package com.seatwise.booking.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  public boolean lockSeat(Long memberId, List<Long> seatIds) {

    Map<String, String> lockMap = new HashMap<>();
    for (Long seatId : seatIds) {
      String key = LOCK_PREFIX + seatId;
      lockMap.put(key, memberId.toString());
    }

    Boolean isLocked = redisTemplate.opsForValue().multiSetIfAbsent(lockMap);
    if (Boolean.TRUE.equals(isLocked)) {
      for (String key : lockMap.keySet()) {
        redisTemplate.expire(key, LOCK_DURATION, TimeUnit.MINUTES);
      }
    }

    return Boolean.TRUE.equals(isLocked);
  }
}
