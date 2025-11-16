package com.seatwise.booking.exception;

import com.seatwise.booking.dto.response.BookingStatusResponse;
import com.seatwise.core.web.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class BookingExceptionHandler {

  @ExceptionHandler(RecoverableBookingException.class)
  public ApiResponse<BookingStatusResponse> handleRecoverable(RecoverableBookingException e) {
    log.warn("Handled {} : {}", e.getClass().getSimpleName(), e.getBaseCode().name(), e);
    return ApiResponse.error(e.getBaseCode());
  }

  @ExceptionHandler(FatalBookingException.class)
  public ApiResponse<Void> handleFatal(FatalBookingException e) {
    log.warn("Handled {} : {}", e.getClass().getSimpleName(), e.getBaseCode().name(), e);
    return ApiResponse.error(e.getBaseCode());
  }
}
