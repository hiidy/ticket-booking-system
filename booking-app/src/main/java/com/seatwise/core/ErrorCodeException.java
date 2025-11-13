package com.seatwise.core;

import lombok.Getter;

@Getter
public abstract class ErrorCodeException extends RuntimeException {
  private final ErrorCode errorCode;

  protected ErrorCodeException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}
