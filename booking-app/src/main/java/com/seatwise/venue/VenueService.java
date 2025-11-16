package com.seatwise.venue;

import com.seatwise.core.BusinessException;
import com.seatwise.core.BaseCode;
import com.seatwise.venue.entity.Venue;
import com.seatwise.venue.entity.VenueRepository;
import com.seatwise.venue.dto.request.VenueCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VenueService {

  private final VenueRepository venueRepository;

  public Venue findById(Long id) {
    return venueRepository
        .findById(id)
        .orElseThrow(() -> new BusinessException(BaseCode.VENUE_NOT_FOUND));
  }

  public Long createVenue(VenueCreateRequest request) {
    Venue venue = new Venue(request.name(), request.totalSeats());
    return venueRepository.save(venue).getId();
  }
}
