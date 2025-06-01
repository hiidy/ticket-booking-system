package com.seatwise.showtime.dto.response;

import com.seatwise.showtime.domain.ShowTime;
import java.time.LocalDate;
import java.time.LocalTime;

public record ShowDatesResponse(Long showId, LocalDate date, LocalTime startTime) {

  public static ShowDatesResponse from(ShowTime showTime) {
    return new ShowDatesResponse(showTime.getId(), showTime.getDate(), showTime.getStartTime());
  }
}
