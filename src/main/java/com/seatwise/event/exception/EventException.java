package com.seatwise.event.exception;

import com.seatwise.global.exception.BusinessException;
import com.seatwise.global.exception.ErrorCode;

public class EventException extends BusinessException {

  public EventException(ErrorCode errorCode) {
    super(errorCode);
  }
}
