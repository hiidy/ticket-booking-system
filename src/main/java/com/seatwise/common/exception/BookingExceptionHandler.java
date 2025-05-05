package com.seatwise.common.exception;

import com.seatwise.booking.dto.BookingResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class BookingExceptionHandler {

  @ExceptionHandler(BookingException.class)
  public ResponseEntity<BookingResult> bookingResultHandler(BookingException e) {
    log.warn("Handled {} : {}", e.getClass().getSimpleName(), e.getErrorCode().name(), e);
    return ResponseEntity.status(e.getErrorCode().getStatus())
        .body(BookingResult.failed(e.getRequestId()));
  }
}
