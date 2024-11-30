package com.seatwise.venue.service;

import com.seatwise.venue.domain.Venue;
import com.seatwise.venue.repository.VenueRepository;
import com.seatwise.venue.service.dto.VenueCreateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VenueService {

  private final VenueRepository venueRepository;

  public Long createVenue(VenueCreateDto createDto) {
    Venue venue =
        venueRepository.save(
            Venue.builder().name(createDto.name()).totalSeats(createDto.totalSeats()).build());
    return venue.getId();
  }
}
