package com.seatwise.common.exception;

public record ErrorResponse(ErrorCode errorCode, String message) {

  public static ErrorResponse from(ErrorCodeException e) {
    return new ErrorResponse(e.getErrorCode(), e.getErrorCode().getMessage());
  }
}
