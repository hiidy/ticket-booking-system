package com.seatwise.core.exception;

import com.seatwise.core.BaseCode;
import lombok.Getter;

@Getter
public abstract class BaseCodeException extends RuntimeException {
  private final BaseCode baseCode;

  protected BaseCodeException(BaseCode baseCode) {
    super(baseCode.getMessage());
    this.baseCode = baseCode;
  }
}
