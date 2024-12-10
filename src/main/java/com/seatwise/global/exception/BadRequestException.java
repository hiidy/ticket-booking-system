package com.seatwise.global.exception;

public class BadRequestException extends BusinessException {
  public BadRequestException(ErrorCode errorCode) {
    super(errorCode);
  }
}
