package com.seatwise.common.builder;

import com.seatwise.event.domain.Event;
import com.seatwise.show.domain.Show;
import com.seatwise.show.repository.ShowRepository;
import com.seatwise.venue.domain.Venue;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShowTestDataBuilder {

  @Autowired private ShowRepository showRepository;
  private Event event;
  private Venue venue;
  private LocalDate date;
  private LocalTime startTime;
  private LocalTime endTime;

  public ShowTestDataBuilder withEvent(Event event) {
    this.event = event;
    return this;
  }

  public ShowTestDataBuilder withDate(LocalDate date) {
    this.date = date;
    return this;
  }

  public ShowTestDataBuilder withTime(LocalTime startTime, LocalTime endTime) {
    this.startTime = startTime;
    this.endTime = endTime;
    return this;
  }

  public Show build() {
    Show show = new Show(event, venue, date, startTime, endTime);
    return showRepository.save(show);
  }
}
