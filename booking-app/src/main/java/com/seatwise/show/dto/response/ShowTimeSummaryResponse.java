package com.seatwise.show.dto.response;

import com.seatwise.show.entity.ShowTime;
import java.time.LocalDate;
import java.time.LocalTime;

public record ShowTimeSummaryResponse(Long showTimeId, LocalDate date, LocalTime startTime) {

  public static ShowTimeSummaryResponse from(ShowTime showTime) {
    return new ShowTimeSummaryResponse(
        showTime.getId(), showTime.getDate(), showTime.getStartTime());
  }
}
