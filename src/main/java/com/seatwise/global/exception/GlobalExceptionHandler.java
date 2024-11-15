package com.seatwise.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
    log.warn(e.getClass().getName(), e);
    ErrorResponse errorResponse = new ErrorResponse(e.getErrorCode(), e.getMessage());
    return ResponseEntity.status(e.getErrorCode().getStatus()).body(errorResponse);
  }
}
