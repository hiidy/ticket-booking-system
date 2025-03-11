package com.seatwise.booking.repository;

import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisBookingRepository {

  private final RedisTemplate<String, String> redisTemplate;
  private final RedissonClient redissonClient;
  private static final String LOCK_PREFIX = "lock:showSeat:";
  private static final long LOCK_DURATION = 10;
  private static final long WAIT_TIME = 3;

  public boolean lockSeat(Long memberId, List<Long> seatIds) {

    List<RLock> locks =
        seatIds.stream().map(seatId -> LOCK_PREFIX + seatId).map(redissonClient::getLock).toList();

    RLock multiLock = redissonClient.getMultiLock(locks.toArray(new RLock[0]));

    try {
      boolean isLocked = multiLock.tryLock(WAIT_TIME, LOCK_DURATION * 60, TimeUnit.SECONDS);
      if (isLocked) {
        for (Long seatId : seatIds) {
          String key = LOCK_PREFIX + seatId;
          redisTemplate
              .opsForValue()
              .set(key, memberId.toString(), LOCK_DURATION, TimeUnit.MINUTES);
        }
        return true;
      }
      return false;

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    }
  }
}
