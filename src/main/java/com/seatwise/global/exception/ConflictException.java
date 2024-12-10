package com.seatwise.global.exception;

public class ConflictException extends BusinessException {
  public ConflictException(ErrorCode errorCode) {
    super(errorCode);
  }
}
