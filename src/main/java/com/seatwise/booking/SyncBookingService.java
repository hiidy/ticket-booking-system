package com.seatwise.booking;

import com.seatwise.booking.exception.RecoverableBookingException;
import com.seatwise.core.ErrorCode;
import com.seatwise.ticket.TicketCacheService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncBookingService {

  private final TicketCacheService cacheService;
  private final BookingService bookingService;
  private final RedissonClient redissonClient;
  private static final long LOCK_WAIT_TIME = 2;
  private static final long LOCK_LEASE_TIME = 10;
  private static final TimeUnit LOCK_TIME_UNIT = TimeUnit.SECONDS;

  public Long createBookingSync(UUID requestId, Long memberId, List<Long> ticketIds) {
    if (cacheService.hasUnavailableTickets(ticketIds, memberId)) {
      throw new RecoverableBookingException(ErrorCode.SEAT_NOT_AVAILABLE, requestId);
    }
    return bookingService.createBookingWithLock(requestId, memberId, ticketIds);
  }

  public Long createBookingSyncWithLock(UUID requestId, Long memberId, List<Long> ticketIds) {
    if (cacheService.hasUnavailableTickets(ticketIds, memberId)) {
      throw new RecoverableBookingException(ErrorCode.SEAT_NOT_AVAILABLE, requestId);
    }
    return createBookingWithRedisLock(requestId, memberId, ticketIds);
  }

  public Long createBookingWithRedisLock(UUID requestId, Long memberId, List<Long> ticketIds) {
    List<RLock> locks =
        ticketIds.stream().sorted().map(id -> redissonClient.getLock("lock:seat:" + id)).toList();

    List<RLock> acquiredLocks = new ArrayList<>();

    try {
      for (RLock lock : locks) {
        boolean acquired = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, LOCK_TIME_UNIT);
        if (!acquired) {
          throw new RecoverableBookingException(ErrorCode.SEAT_NOT_AVAILABLE, requestId);
        }
        acquiredLocks.add(lock);
      }

      return bookingService.createBooking(requestId, memberId, ticketIds);

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RecoverableBookingException(ErrorCode.SEAT_NOT_AVAILABLE, requestId);
    } finally {
      unlockAll(acquiredLocks);
    }
  }

  private void unlockAll(List<RLock> acquiredLocks) {
    for (RLock lock : acquiredLocks) {
      try {
        if (lock.isHeldByCurrentThread()) {
          lock.unlock();
        }
      } catch (Exception e) {
        log.error("Failed to unlock: {}", lock.getName(), e);
      }
    }
  }
}
