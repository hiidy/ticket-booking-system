package com.seatwise.core.web;

import com.seatwise.booking.dto.BookingResult;
import com.seatwise.booking.exception.BookingException;
import com.seatwise.core.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleErrorCodeException(BusinessException e) {
    HttpStatus status = ErrorCodeToStatusMapper.getHttpStatus(e.getErrorCode());
    log.warn("Handled {} : {}", e.getClass().getSimpleName(), e.getErrorCode().name(), e);
    return ResponseEntity.status(status).body(ErrorResponse.from(e));
  }

  @ExceptionHandler(BookingException.class)
  public ResponseEntity<BookingResult> bookingResultHandler(BookingException e) {
    HttpStatus status = ErrorCodeToStatusMapper.getHttpStatus(e.getErrorCode());
    log.warn("Handled {} : {}", e.getClass().getSimpleName(), e.getErrorCode().name(), e);
    return ResponseEntity.status(status).body(BookingResult.failed(e.getRequestId()));
  }
}
