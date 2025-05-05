package com.seatwise.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

  // 400 Bad Request
  INVALID_SHOW_TIME("Show의 종료 시각은 시작 시각 이후여야 합니다.", HttpStatus.BAD_REQUEST),
  SEAT_NOT_AVAILABLE("좌석이 예매할 수 있는 상태가 아닙니다.", HttpStatus.BAD_REQUEST),
  SEAT_ALREADY_BOOKED("이미 예매된 좌석입니다.", HttpStatus.BAD_REQUEST),
  INVALID_SEAT_PRICE("좌석의 가격은 0 이상이어야 합니다.", HttpStatus.BAD_REQUEST),
  NO_AVAILABLE_STOCK("재고가 0개입니다.", HttpStatus.BAD_REQUEST),

  // 404 Not Found
  EVENT_NOT_FOUND("이벤트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  VENUE_NOT_FOUND("장소를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  SHOW_NOT_FOUND("진행 시간을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  SHOW_SEAT_NOT_FOUND("ShowSeat를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  MEMBER_NOT_FOUND("유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

  // 409 Conflict
  DUPLICATE_SEAT_NUMBER("중복된 좌석 번호입니다.", HttpStatus.CONFLICT),
  DUPLICATE_SHOW("이벤트 시간이 중복됐습니다.", HttpStatus.CONFLICT),
  DUPLICATE_IDEMPOTENCY_KEY(
      "이미 처리된 요청입니다. 같은 Idempotency-Key로는 중복 요청이 불가능합니다.", HttpStatus.CONFLICT),

  // 502 Bad Gateway
  BOOKING_TIMEOUT("좌석 예약 요청이 시간 초과로 실패했습니다.", HttpStatus.GATEWAY_TIMEOUT);

  private final String message;
  private final HttpStatus status;

  ErrorCode(String message, HttpStatus status) {
    this.message = message;
    this.status = status;
  }
}
