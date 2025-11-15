package com.seatwise.show.dto.response;

import com.seatwise.show.entity.Show;

public record ShowCreateResponse(Long id) {

  public static ShowCreateResponse from(Show show) {
    return new ShowCreateResponse(show.getId());
  }
}
