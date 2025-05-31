package com.seatwise.show.dto.response;

import com.seatwise.show.entity.Show;
import java.time.LocalDate;
import java.time.LocalTime;

public record ShowDatesResponse(Long showId, LocalDate date, LocalTime startTime) {

  public static ShowDatesResponse from(Show show) {
    return new ShowDatesResponse(show.getId(), show.getDate(), show.getStartTime());
  }
}
