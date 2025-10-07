package com.seatwise.booking.exception;

import com.seatwise.core.ErrorCode;
import java.util.UUID;

public class RecoverableBookingException extends BookingException {
  public RecoverableBookingException(ErrorCode errorCode, UUID requestId) {
    super(errorCode, requestId);
  }
}
