package com.seatwise.showtime.dto.response;

import com.seatwise.showtime.ShowTime;
import java.time.LocalDate;
import java.time.LocalTime;

public record ShowTimeSummaryResponse(Long showTimeId, LocalDate date, LocalTime startTime) {

  public static ShowTimeSummaryResponse from(ShowTime showTime) {
    return new ShowTimeSummaryResponse(
        showTime.getId(), showTime.getDate(), showTime.getStartTime());
  }
}
