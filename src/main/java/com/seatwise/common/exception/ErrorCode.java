package com.seatwise.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
  INVALID_SHOW_TIME("Show의 종료 시간은 시작시간 이후여야 합니다."),

  EVENT_NOT_FOUND("이벤트를 찾을 수 없습니다."),
  VENUE_NOT_FOUND("장소를 찾을 수 없습니다"),
  SHOW_NOT_FOUND("진행 시간을 찾을 수 없습니다"),
  SHOW_SEAT_NOT_FOUND("ShowSeat를 찾을 수 없습니다."),

  DUPLICATE_SHOW("이벤트 시간이 중복됐습니다.");

  private final String message;

  ErrorCode(String message) {
    this.message = message;
  }
}
