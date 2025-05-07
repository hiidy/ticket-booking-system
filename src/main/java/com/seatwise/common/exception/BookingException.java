package com.seatwise.common.exception;

import java.util.UUID;
import lombok.Getter;

@Getter
public class BookingException extends ErrorCodeException {

  private final UUID requestId;

  public BookingException(ErrorCode errorCode, UUID requestId) {
    super(errorCode);
    this.requestId = requestId;
  }
}
