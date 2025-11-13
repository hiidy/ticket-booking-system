package com.seatwise.booking.exception;

import com.seatwise.core.ErrorCode;
import java.util.UUID;

public class FatalBookingException extends BookingException {

  public FatalBookingException(ErrorCode errorCode, UUID requestId) {
    super(errorCode, requestId);
  }
}
