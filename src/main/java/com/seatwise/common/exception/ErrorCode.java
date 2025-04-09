package com.seatwise.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
  INVALID_SHOW_TIME("Show의 종료 시각은 시작 시각 이후여야 합니다."),
  SEAT_NOT_AVAILABLE("좌석이 예매할 수 있는 상태가 아닙니다."),
  SEAT_ALREADY_BOOKED("이미 예매된 좌석입니다."),
  INVALID_SEAT_PRICE("좌석의 가격은 0 이상이어야 합니다."),
  NO_AVAILABLE_STOCK("재고가 0개입니다."),

  EVENT_NOT_FOUND("이벤트를 찾을 수 없습니다."),
  VENUE_NOT_FOUND("장소를 찾을 수 없습니다."),
  SHOW_NOT_FOUND("진행 시간을 찾을 수 없습니다."),
  SHOW_SEAT_NOT_FOUND("ShowSeat를 찾을 수 없습니다."),
  MEMBER_NOT_FOUND("유저를 찾을 수 없습니다."),

  DUPLICATE_SEAT_NUMBER("중복된 좌석 번호입니다."),
  DUPLICATE_SHOW("이벤트 시간이 중복됐습니다."),

  SEAT_ALREADY_LOCKED("좌석이 이미 선점되었습니다. 다른 좌석을 이용해주세요");

  private final String message;

  ErrorCode(String message) {
    this.message = message;
  }
}
