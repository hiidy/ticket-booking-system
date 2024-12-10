package com.seatwise.global.exception;

public record ErrorResponse(ErrorCode errorCode, String message) {

  public static ErrorResponse from(BusinessException e) {
    return new ErrorResponse(e.getErrorCode(), e.getErrorCode().getMessage());
  }
}
