package com.seatwise.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException e) {
    log.warn(e.getClass().getName(), e);
    return ResponseEntity.badRequest().body(ErrorResponse.from(e));
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException e) {
    log.warn(e.getClass().getName(), e);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.from(e));
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ErrorResponse> handleConflictException(ConflictException e) {
    log.warn(e.getClass().getName(), e);
    return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.from(e));
  }
}
