package com.seatwise.showtime.dto.response;

import com.seatwise.showtime.ShowTime;

public record ShowTimeCreateResponse(Long id, Long eventId) {

  public static ShowTimeCreateResponse from(ShowTime showTime) {
    return new ShowTimeCreateResponse(showTime.getId(), showTime.getShow().getId());
  }
}
