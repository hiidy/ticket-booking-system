package com.seatwise.common.exception;

public class ConflictException extends BusinessException {
  public ConflictException(ErrorCode errorCode) {
    super(errorCode);
  }
}
