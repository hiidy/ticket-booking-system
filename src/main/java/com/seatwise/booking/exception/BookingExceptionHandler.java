package com.seatwise.booking.exception;

import com.seatwise.booking.dto.response.BookingResponse;
import com.seatwise.core.web.ErrorCodeToStatusMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class BookingExceptionHandler {

  @ExceptionHandler(BookingException.class)
  public ResponseEntity<BookingResponse> bookingResultHandler(BookingException e) {
    HttpStatus status = ErrorCodeToStatusMapper.getHttpStatus(e.getErrorCode());
    log.warn("Handled {} : {}", e.getClass().getSimpleName(), e.getErrorCode().name(), e);
    return ResponseEntity.status(status).body(BookingResponse.failed(e.getRequestId()));
  }
}
