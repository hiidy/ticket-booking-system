package com.seatwise.common.exception;

public class BadRequestException extends BusinessException {
  public BadRequestException(ErrorCode errorCode) {
    super(errorCode);
  }
}
