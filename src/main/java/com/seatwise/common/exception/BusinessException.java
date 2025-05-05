package com.seatwise.common.exception;

public class BusinessException extends ErrorCodeException {

  public BusinessException(ErrorCode errorCode) {
    super(errorCode);
  }
}
