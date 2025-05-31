package com.seatwise.showtime.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {
  AVAILABLE("예매 가능"),
  PAYMENT_PENDING("결제 대기중"),
  BOOKED("예매 완료"),
  CANCELLED("예매 취소");

  public final String description;
}
