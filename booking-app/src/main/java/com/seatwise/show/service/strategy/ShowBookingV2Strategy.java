package com.seatwise.show.service.strategy;

import com.seatwise.booking.exception.RecoverableBookingException;
import com.seatwise.core.BaseCode;
import com.seatwise.show.dto.request.ShowBookingRequest;
import com.seatwise.show.service.ShowBookingService;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

@Slf4j
@ShowBookingStrategyVersion("v2")
@RequiredArgsConstructor
public class ShowBookingV2Strategy implements ShowBookingStrategy {

  private final ShowBookingService showBookingService;
  private final RedissonClient redissonClient;

  private static final long LOCK_WAIT_TIME = 0;
  private static final long LOCK_LEASE_TIME = 300;
  private static final TimeUnit LOCK_TIME_UNIT = TimeUnit.SECONDS;

  @Override
  public String createBooking(UUID idempotencyKey, ShowBookingRequest request) {
    RLock multiLock = acquireMultiLock(request.ticketIds());

    try {
      if (!multiLock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, LOCK_TIME_UNIT)) {
        throw new RecoverableBookingException(BaseCode.SEAT_NOT_AVAILABLE, idempotencyKey);
      }

      return showBookingService.create(idempotencyKey, request.memberId(), request.ticketIds());

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("락 획득 중 인터럽트 발생: requestId={}", idempotencyKey);
      throw new RecoverableBookingException(BaseCode.LOCK_ACQUISITION_TIMEOUT, idempotencyKey);
    } finally {
      safeUnlock(multiLock);
    }
  }

  private RLock acquireMultiLock(List<Long> ticketIds) {
    return redissonClient.getMultiLock(
        ticketIds.stream()
            .sorted()
            .map(id -> redissonClient.getLock("lock:seat:" + id))
            .toArray(RLock[]::new));
  }

  private void safeUnlock(RLock lock) {
    try {
      lock.unlock();
    } catch (Exception e) {
      log.error("락 해제 중 예외 발생", e);
    }
  }
}
