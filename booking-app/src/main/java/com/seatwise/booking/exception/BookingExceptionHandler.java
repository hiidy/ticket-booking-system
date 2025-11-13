package com.seatwise.booking.exception;

import com.seatwise.booking.dto.response.BookingStatusResponse;
import com.seatwise.core.web.ErrorCodeToStatusMapper;
import com.seatwise.core.web.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class BookingExceptionHandler {

  @ExceptionHandler(RecoverableBookingException.class)
  public ResponseEntity<BookingStatusResponse> handleRecoverable(RecoverableBookingException e) {
    HttpStatus status = ErrorCodeToStatusMapper.getHttpStatus(e.getErrorCode());
    return ResponseEntity.status(status).body(BookingStatusResponse.failed(e.getRequestId()));
  }

  @ExceptionHandler(FatalBookingException.class)
  public ResponseEntity<ErrorResponse> handleFatal(FatalBookingException e) {
    HttpStatus status = ErrorCodeToStatusMapper.getHttpStatus(e.getErrorCode());
    log.warn("Handled {} : {}", e.getClass().getSimpleName(), e.getErrorCode().name(), e);
    return ResponseEntity.status(status).body(ErrorResponse.from(e));
  }
}
