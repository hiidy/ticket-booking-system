package com.seatwise.show.dto.response;

import com.seatwise.show.entity.ShowTime;

public record ShowTimeCreateResponse(Long id, Long eventId) {

  public static ShowTimeCreateResponse from(ShowTime showTime) {
    return new ShowTimeCreateResponse(showTime.getId(), showTime.getShow().getId());
  }
}
