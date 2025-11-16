package com.seatwise.core.exception;

import com.seatwise.core.BaseCode;

public class BusinessException extends BaseCodeException {

  public BusinessException(BaseCode baseCode) {
    super(baseCode);
  }
}
