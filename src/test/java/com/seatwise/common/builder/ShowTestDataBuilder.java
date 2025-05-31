package com.seatwise.common.builder;

import com.seatwise.event.entity.Event;
import com.seatwise.showtime.entity.ShowTime;
import com.seatwise.showtime.repository.ShowTimeRepository;
import com.seatwise.venue.entity.Venue;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShowTestDataBuilder {

  @Autowired private ShowTimeRepository showTimeRepository;
  private Event event;
  private Venue venue;
  private LocalDate date;
  private LocalTime startTime;
  private LocalTime endTime;

  public ShowTestDataBuilder withEvent(Event event) {
    this.event = event;
    return this;
  }

  public ShowTestDataBuilder withVenue(Venue venue) {
    this.venue = venue;
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

  public ShowTime build() {
    ShowTime showTime = new ShowTime(event, venue, date, startTime, endTime);
    return showTimeRepository.save(showTime);
  }
}
