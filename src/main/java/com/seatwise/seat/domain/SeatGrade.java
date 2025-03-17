package com.seatwise.seat.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SeatGrade {
  VIP("VIP석"),
  PREMIUM("프리미엄석"),
  R("R석"),
  S("S석"),
  A("A석"),
  B("B석");

  private final String description;
}
