package com.seatwise.booking;

import com.seatwise.booking.dto.BookingResult;
import com.seatwise.booking.exception.BookingException;
import com.seatwise.core.ErrorCode;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingResultWaitService {

  private static final long WAIT_TIMEOUT = 10000L;
  private final Map<UUID, DeferredResult<BookingResult>> waiters = new ConcurrentHashMap<>();

  public DeferredResult<BookingResult> waitForResult(UUID requestId) {
    DeferredResult<BookingResult> deferredResult = new DeferredResult<>(WAIT_TIMEOUT);
    waiters.put(requestId, deferredResult);

    deferredResult.onTimeout(
        () -> {
          waiters.remove(requestId);
          log.warn("requestId {} - 타임아웃 발생", requestId);
          deferredResult.setErrorResult(new BookingException(ErrorCode.BOOKING_TIMEOUT, requestId));
        });
    log.info("결과 있음{}", deferredResult.hasResult());
    return deferredResult;
  }

  public void completeResult(UUID requestId, BookingResult result) {
    DeferredResult<BookingResult> deferredResult = waiters.remove(requestId);
    if (deferredResult != null) {
      deferredResult.setResult(result);
      log.info("requestId {} - 응답 완료", requestId);
    } else {
      log.warn("requestId {} - 대기 중인 요청이 없음", requestId);
    }
  }

  public void completeWithFailure(UUID requestId, BookingException e) {
    DeferredResult<BookingResult> deferredResult = waiters.remove(requestId);
    if (deferredResult != null) {
      deferredResult.setErrorResult(e);
      log.warn("requestId {} - 예외로 응답 완료: {}", requestId, e.getErrorCode());
    } else {
      log.warn("requestId {} - 대기 중인 요청이 없어 실패 응답 불가", requestId);
    }
  }
}
