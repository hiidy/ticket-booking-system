package com.seatwise.ticket;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TicketStatus {
  AVAILABLE("예매 가능"),
  PAYMENT_PENDING("결제 대기중"),
  BOOKED("예매 완료"),
  CANCELLED("예매 취소");

  public final String description;
}
