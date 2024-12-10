package com.seatwise.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
  INVALID_SHOW_TIME(HttpStatus.BAD_REQUEST, "Show의 종료 시간은 시작시간 이후여야 합니다."),

  EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "이벤트를 찾을 수 없습니다."),
  VENUE_NOT_FOUND(HttpStatus.NOT_FOUND, "장소를 찾을 수 없습니다"),
  SHOW_NOT_FOUND(HttpStatus.NOT_FOUND, "진행 시간을 찾을 수 없습니다"),

  DUPLICATE_SHOW(HttpStatus.CONFLICT, "이벤트 시간이 중복됐습니다.");

  private final HttpStatus httpStatus;
  private final String message;

  ErrorCode(HttpStatus httpStatus, String message) {
    this.httpStatus = httpStatus;
    this.message = message;
  }
}
