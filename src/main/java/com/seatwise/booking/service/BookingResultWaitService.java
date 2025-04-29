package com.seatwise.booking.service;

import com.seatwise.booking.dto.BookingResult;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingResultWaitService {

  private final Map<String, DeferredResult<BookingResult>> waiters = new ConcurrentHashMap<>();
  private static final long WAIT_TIMEOUT = 10000L;

  public DeferredResult<BookingResult> waitForResult(String requestId) {
    DeferredResult<BookingResult> deferredResult = new DeferredResult<>(WAIT_TIMEOUT);
    waiters.put(requestId, deferredResult);

    deferredResult.onTimeout(
        () -> {
          waiters.remove(requestId);
          log.warn("requestId {} - 타임아웃 발생", requestId);
          deferredResult.setErrorResult(BookingResult.failed(requestId));
        });
    log.info("결과 있음{}", deferredResult.hasResult());
    return deferredResult;
  }

  public void completeResult(String requestId, BookingResult result) {
    DeferredResult<BookingResult> deferredResult = waiters.remove(requestId);
    if (deferredResult != null) {
      deferredResult.setResult(result);
      log.info("requestId {} - 응답 완료", requestId);
    } else {
      log.warn("requestId {} - 대기 중인 요청이 없음", requestId);
    }
  }
}
