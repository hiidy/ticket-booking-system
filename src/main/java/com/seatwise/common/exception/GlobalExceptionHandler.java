package com.seatwise.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
    log.warn("BusinessException 발생 : {}", e.getErrorCode().name(), e);
    return ResponseEntity.status(e.getErrorCode().getStatus()).body(ErrorResponse.from(e));
  }
}
