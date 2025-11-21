package com.seatwise.show.dto.request;

import com.seatwise.show.entity.Show;
import com.seatwise.show.entity.ShowType;
import com.seatwise.venue.entity.Venue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record ShowRequest(
    @NotBlank String title,
    @NotBlank String description,
    @NotBlank String eventType,
    @NotNull Venue venue,
    @NotNull LocalDate date,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime) {

  public Show toEvent() {
    return new Show(
        title, description, ShowType.valueOf(eventType), venue, date, startTime, endTime);
  }
}
