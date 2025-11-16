package com.seatwise.booking.exception;

import com.seatwise.core.BaseCode;
import java.util.UUID;

public class RecoverableBookingException extends BookingException {
  public RecoverableBookingException(BaseCode baseCode, UUID requestId) {
    super(baseCode, requestId);
  }
}
