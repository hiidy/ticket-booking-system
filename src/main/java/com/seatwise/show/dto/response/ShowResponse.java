package com.seatwise.show.dto.response;

import com.seatwise.show.domain.Show;
import java.time.LocalTime;

public record ShowResponse(LocalTime startTime, LocalTime endTime) {

  public static ShowResponse from(Show show) {
    return new ShowResponse(show.getStartTime(), show.getEndTime());
  }
}
