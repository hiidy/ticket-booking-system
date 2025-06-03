package com.seatwise.core.web;

import com.seatwise.core.ErrorCode;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class ErrorCodeToStatusMapper {

  public static final Map<ErrorCode, HttpStatus> MAP = new EnumMap<>(ErrorCode.class);

  static {
    // 400
    MAP.put(ErrorCode.INVALID_SHOW_TIME, HttpStatus.BAD_REQUEST);
    MAP.put(ErrorCode.SEAT_NOT_AVAILABLE, HttpStatus.BAD_REQUEST);
    MAP.put(ErrorCode.SEAT_ALREADY_BOOKED, HttpStatus.BAD_REQUEST);
    MAP.put(ErrorCode.INVALID_SEAT_PRICE, HttpStatus.BAD_REQUEST);
    MAP.put(ErrorCode.NO_AVAILABLE_STOCK, HttpStatus.BAD_REQUEST);

    // 404
    MAP.put(ErrorCode.EVENT_NOT_FOUND, HttpStatus.NOT_FOUND);
    MAP.put(ErrorCode.VENUE_NOT_FOUND, HttpStatus.NOT_FOUND);
    MAP.put(ErrorCode.SHOW_NOT_FOUND, HttpStatus.NOT_FOUND);
    MAP.put(ErrorCode.TICKET_NOT_FOUND, HttpStatus.NOT_FOUND);
    MAP.put(ErrorCode.MEMBER_NOT_FOUND, HttpStatus.NOT_FOUND);

    // 409
    MAP.put(ErrorCode.DUPLICATE_SEAT_NUMBER, HttpStatus.CONFLICT);
    MAP.put(ErrorCode.DUPLICATE_SHOW, HttpStatus.CONFLICT);
    MAP.put(ErrorCode.DUPLICATE_IDEMPOTENCY_KEY, HttpStatus.CONFLICT);

    // 502
    MAP.put(ErrorCode.BOOKING_TIMEOUT, HttpStatus.GATEWAY_TIMEOUT);
  }

  public static HttpStatus getHttpStatus(ErrorCode code) {
    return MAP.getOrDefault(code, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
