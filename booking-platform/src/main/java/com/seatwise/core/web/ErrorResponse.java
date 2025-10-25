package com.seatwise.core.web;

import com.seatwise.core.ErrorCode;
import com.seatwise.core.ErrorCodeException;

public record ErrorResponse(ErrorCode errorCode, String message) {

  public static ErrorResponse from(ErrorCodeException e) {
    return new ErrorResponse(e.getErrorCode(), e.getErrorCode().getMessage());
  }
}
