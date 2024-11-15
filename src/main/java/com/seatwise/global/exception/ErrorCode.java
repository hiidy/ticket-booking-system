package com.seatwise.global.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
  EVENT_NOT_FOUND(404, "이벤트를 찾을 수 없습니다.");

  private final int status;
  private final String message;

  ErrorCode(int status, String message) {
    this.status = status;
    this.message = message;
  }
}
