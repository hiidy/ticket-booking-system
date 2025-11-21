package com.seatwise.support;

import com.seatwise.show.entity.Show;
import com.seatwise.show.entity.ShowType;
import com.seatwise.show.repository.ShowRepository;
import com.seatwise.venue.entity.Venue;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShowTestDataBuilder {

  @Autowired private ShowRepository showRepository;
  private String title = "Default Show";
  private String description = "Default Description";
  private ShowType type = ShowType.CONCERT;
  private Venue venue;
  private LocalDate date;
  private LocalTime startTime;
  private LocalTime endTime;

  public ShowTestDataBuilder(ShowRepository showRepository) {
    this.showRepository = showRepository;
  }

  public ShowTestDataBuilder withTitle(String title) {
    this.title = title;
    return this;
  }

  public ShowTestDataBuilder withDescription(String description) {
    this.description = description;
    return this;
  }

  public ShowTestDataBuilder withType(ShowType type) {
    this.type = type;
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

  public Show build() {
    Show show = new Show(title, description, type, venue, date, startTime, endTime);
    return showRepository.save(show);
  }
}
