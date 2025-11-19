package com.seatwise.show.service.strategy;

import com.seatwise.booking.dto.request.BookingRequest;
import com.seatwise.booking.exception.RecoverableBookingException;
import com.seatwise.core.BaseCode;
import com.seatwise.show.cache.local.LocalLock;
import com.seatwise.show.service.ShowBookingService;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

@Slf4j
@BookingStrategyVersion("v3")
@RequiredArgsConstructor
public class BookingV3Strategy implements BookingStrategy {

  private final ShowBookingService showBookingService;
  private final RedissonClient redissonClient;
  private final LocalLock localLock = new LocalLock();

  private static final long LOCK_WAIT_TIME = 0;
  private static final long LOCK_LEASE_TIME = 300;
  private static final TimeUnit LOCK_TIME_UNIT = TimeUnit.SECONDS;

  @Override
  public String createBooking(UUID idempotencyKey, BookingRequest request) {
    String lockKey = "section:" + request.sectionId();
    ReentrantLock sectionLocalLock = this.localLock.getLock(lockKey);

    try {
      sectionLocalLock.lock();

      RLock multiLock =
          redissonClient.getMultiLock(
              request.sectionId().toString(), new ArrayList<>(request.ticketIds()));

      try {
        if (!multiLock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, LOCK_TIME_UNIT)) {
          throw new RecoverableBookingException(BaseCode.SEAT_NOT_AVAILABLE, idempotencyKey);
        }

        return showBookingService.create(idempotencyKey, request.memberId(), request.ticketIds());

      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.error("Redis 락 획득 중 인터럽트 발생: requestId={}", idempotencyKey);
        throw new RecoverableBookingException(BaseCode.LOCK_ACQUISITION_TIMEOUT, idempotencyKey);
      } finally {
        safeUnlock(multiLock);
      }

    } finally {
      try {
        sectionLocalLock.unlock();
      } catch (Exception e) {
        log.error("로컬 락 해제 중 예외 발생", e);
      }
    }
  }

  private void safeUnlock(RLock lock) {
    try {
      lock.unlock();
    } catch (Exception e) {
      log.error("락 해제 중 예외 발생", e);
    }
  }
}
