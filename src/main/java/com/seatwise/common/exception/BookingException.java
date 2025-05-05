package com.seatwise.common.exception;

import lombok.Getter;

@Getter
public class BookingException extends BusinessException {

  private final String requestId;

  public BookingException(ErrorCode errorCode, String requestId) {
    super(errorCode);
    this.requestId = requestId;
  }
}
