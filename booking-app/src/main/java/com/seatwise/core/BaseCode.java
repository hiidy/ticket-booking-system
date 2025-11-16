package com.seatwise.core;

import lombok.Getter;

/**
 * 기본 응답 코드
 */
@Getter
public enum BaseCode {

  // 기본 코드
  SUCCESS(0, "성공"),
  SYSTEM_ERROR(-1, "시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),

  // 2000-2999: 파라미터/유효성 검사 관련
  INVALID_SHOW_TIME(2001, "Show의 종료 시각은 시작 시각 이후여야 합니다."),
  INVALID_SEAT_PRICE(2002, "좌석의 가격은 0 이상이어야 합니다."),
  ARGUMENT_EMPTY(2003, "필수 파라미터가 비어있습니다."),

  // 3000-3999: 공연/프로그램 관련
  EVENT_NOT_FOUND(3001, "이벤트를 찾을 수 없습니다."),
  VENUE_NOT_FOUND(3002, "장소를 찾을 수 없습니다."),
  SHOW_NOT_FOUND(3003, "진행 시간을 찾을 수 없습니다."),
  DUPLICATE_SHOW(3004, "이벤트 시간이 중복됐습니다."),

  // 4000-4999: 좌석/예매 관련
  SEAT_NOT_AVAILABLE(4001, "좌석이 예매할 수 있는 상태가 아닙니다."),
  SEAT_ALREADY_BOOKED(4002, "이미 예매된 좌석입니다."),
  TICKET_NOT_FOUND(4003, "티켓을 찾을 수 없습니다."),
  NO_AVAILABLE_STOCK(4004, "재고가 0개입니다."),
  DUPLICATE_SEAT_NUMBER(4005, "중복된 좌석 번호입니다."),
  BOOKING_TIMEOUT(4006, "좌석 예약 요청이 시간 초과로 실패했습니다."),
  LOCK_ACQUISITION_TIMEOUT(4007, "락 획득 타임아웃. 잠시 후 다시 시도해주세요."),

  // 5000-5999: 주문/결제 관련
  DUPLICATE_IDEMPOTENCY_KEY(5001, "이미 처리된 요청입니다. 같은 Idempotency-Key로는 중복 요청이 불가능합니다."),

  // 6000-6999: 사용자/계정 관련
  MEMBER_NOT_FOUND(6001, "회원을 찾을 수 없습니다."),
  USER_NOT_LOGIN(6002, "사용자가 로그인하지 않았습니다.");

  private final Integer code;
  private final String message;

  BaseCode(Integer code, String message) {
    this.code = code;
    this.message = message;
  }

  public static String getMessage(Integer code) {
    for (BaseCode baseCode : BaseCode.values()) {
      if (baseCode.code.equals(code)) {
        return baseCode.message;
      }
    }
    return "알 수 없는 오류가 발생했습니다.";
  }

  public static BaseCode getByCode(Integer code) {
    for (BaseCode baseCode : BaseCode.values()) {
      if (baseCode.code.equals(code)) {
        return baseCode;
      }
    }
    return SYSTEM_ERROR;
  }
}
