package com.seatwise.booking.exception;

import com.seatwise.core.exception.BaseCodeException;
import com.seatwise.core.BaseCode;
import java.util.UUID;
import lombok.Getter;

@Getter
public class BookingException extends BaseCodeException {

  private final UUID requestId;

  public BookingException(BaseCode baseCode, UUID requestId) {
    super(baseCode);
    this.requestId = requestId;
  }
}
