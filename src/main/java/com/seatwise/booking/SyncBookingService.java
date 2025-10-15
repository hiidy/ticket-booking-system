package com.seatwise.booking;

import com.seatwise.booking.exception.RecoverableBookingException;
import com.seatwise.core.ErrorCode;
import com.seatwise.ticket.TicketCacheService;
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
  private static final long LOCK_WAIT_TIME = 0;
  private static final long LOCK_LEASE_TIME = 300;
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
    RLock multiLock =
        redissonClient.getMultiLock(
            ticketIds.stream()
                .sorted()
                .map(id -> redissonClient.getLock("lock:seat:" + id))
                .toArray(RLock[]::new));

    try {
      boolean acquired = multiLock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, LOCK_TIME_UNIT);
      if (!acquired) {
        throw new RecoverableBookingException(ErrorCode.SEAT_NOT_AVAILABLE, requestId);
      }

      return bookingService.createBooking(requestId, memberId, ticketIds);

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("락 도중에 인터럽트 발생 : requestId={}", requestId);
      throw new RecoverableBookingException(ErrorCode.LOCK_ACQUISITION_TIMEOUT, requestId);

    } finally {
      safeUnlock(multiLock);
    }
  }

  private void safeUnlock(RLock lock) {
    try {
      lock.unlock();
    } catch (IllegalMonitorStateException e) {
      log.debug("락이 이미 해제됐거나 다른 스레드에 의해 해제됨");
    } catch (Exception e) {
      log.error("락 도중 예외 발생", e);
    }
  }
}
