package com.seatwise.booking.exception;

import com.seatwise.core.ErrorCodeException;
import com.seatwise.core.ErrorCode;
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
