package com.seatwise.support;

import com.seatwise.show.Show;
import com.seatwise.showtime.ShowTime;
import com.seatwise.showtime.ShowTimeRepository;
import com.seatwise.venue.entity.Venue;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShowTimeTestDataBuilder {

  @Autowired private ShowTimeRepository showTimeRepository;
  private Show show;
  private Venue venue;
  private LocalDate date;
  private LocalTime startTime;
  private LocalTime endTime;

  public ShowTimeTestDataBuilder withEvent(Show show) {
    this.show = show;
    return this;
  }

  public ShowTimeTestDataBuilder withVenue(Venue venue) {
    this.venue = venue;
    return this;
  }

  public ShowTimeTestDataBuilder withDate(LocalDate date) {
    this.date = date;
    return this;
  }

  public ShowTimeTestDataBuilder withTime(LocalTime startTime, LocalTime endTime) {
    this.startTime = startTime;
    this.endTime = endTime;
    return this;
  }

  public ShowTime build() {
    ShowTime showTime = new ShowTime(show, venue, date, startTime, endTime);
    return showTimeRepository.save(showTime);
  }
}
