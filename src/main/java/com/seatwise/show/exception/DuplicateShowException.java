package com.seatwise.show.exception;

import com.seatwise.global.exception.BusinessException;
import com.seatwise.global.exception.ErrorCode;

public class DuplicateShowException extends BusinessException {
  public DuplicateShowException(ErrorCode errorCode) {
    super(errorCode);
  }
}
