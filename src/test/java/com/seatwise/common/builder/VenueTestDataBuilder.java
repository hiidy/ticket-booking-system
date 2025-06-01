package com.seatwise.common.builder;

import com.seatwise.venue.domain.Venue;
import com.seatwise.venue.domain.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VenueTestDataBuilder {

  @Autowired private VenueRepository venueRepository;
  private String name;
  private int totaSeats;

  public VenueTestDataBuilder(VenueRepository venueRepository) {
    this.venueRepository = venueRepository;
  }

  public VenueTestDataBuilder withName(String name) {
    this.name = name;
    return this;
  }

  public VenueTestDataBuilder withToTalSeat(int totaSeats) {
    this.totaSeats = totaSeats;
    return this;
  }

  public Venue build() {
    Venue venue = new Venue(name, totaSeats);
    return venueRepository.save(venue);
  }
}
