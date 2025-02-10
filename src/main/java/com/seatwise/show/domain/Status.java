package com.seatwise.show.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {
  AVAILABLE("예약 가능"),
  PAYMENT_PENDING("결제 대기중"),
  BOOKED("예약 완료"),
  CANCELLED("예약 취소");

  public final String description;
}
