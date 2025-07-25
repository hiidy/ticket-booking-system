package com.seatwise.show;

import lombok.Getter;

@Getter
public enum ShowType {
  CONCERT("콘서트"),
  MUSICAL("뮤지컬"),
  THEATER("연극"),
  EXHIBITION("전시회");

  private final String description;

  ShowType(String description) {
    this.description = description;
  }
}
