package com.seatwise.show.entity;

import lombok.Getter;

@Getter
public enum SectionType {
  G1("그라운드 1층 VIP 섹션"),
  G2("그라운드 2층 VIP 섹션"),
  G3("3층 스탠드 VIP 섹션"),
  P("프리미엄 섹션"),
  R("일반 섹션"),
  S("일반 섹션"),
  A("일반 섹션");

  private final String description;

  SectionType(String description) {
    this.description = description;
  }
}
