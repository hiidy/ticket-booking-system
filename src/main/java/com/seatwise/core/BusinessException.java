package com.seatwise.core;

public class BusinessException extends ErrorCodeException {

  public BusinessException(ErrorCode errorCode) {
    super(errorCode);
  }
}
