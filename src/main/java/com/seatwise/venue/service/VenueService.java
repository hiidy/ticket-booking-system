package com.seatwise.venue.service;

import com.seatwise.common.exception.BusinessException;
import com.seatwise.common.exception.ErrorCode;
import com.seatwise.venue.dto.request.VenueCreateRequest;
import com.seatwise.venue.entity.Venue;
import com.seatwise.venue.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VenueService {

  private final VenueRepository venueRepository;

  public Venue findById(Long id) {
    return venueRepository
        .findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.VENUE_NOT_FOUND));
  }

  public Long createVenue(VenueCreateRequest request) {
    Venue venue = new Venue(request.name(), request.totalSeats());
    return venueRepository.save(venue).getId();
  }
}
