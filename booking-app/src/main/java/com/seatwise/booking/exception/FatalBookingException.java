package com.seatwise.booking.exception;

import com.seatwise.core.BaseCode;
import java.util.UUID;

public class FatalBookingException extends BookingException {

  public FatalBookingException(BaseCode baseCode, UUID requestId) {
    super(baseCode, requestId);
  }
}
