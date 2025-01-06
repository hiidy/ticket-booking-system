package com.seatwise.seat.service;

import com.seatwise.seat.domain.Seat;
import com.seatwise.seat.domain.SeatType;
import com.seatwise.seat.dto.request.SeatCreateRequest;
import com.seatwise.seat.dto.response.SeatCreateResponse;
import com.seatwise.seat.repository.SeatRepository;
import com.seatwise.venue.domain.Venue;
import com.seatwise.venue.service.VenueService;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatService {

  private final SeatRepository seatRepository;
  private final VenueService venueService;

  public SeatCreateResponse createSeat(SeatCreateRequest request) {
    Venue venue = venueService.findById(request.venueId());

    List<Seat> seats =
        request.seatTypeRanges().stream()
            .flatMap(
                range ->
                    IntStream.rangeClosed(range.startNumber(), range.endNumber())
                        .mapToObj(
                            seatNumber ->
                                Seat.builder()
                                    .venue(venue)
                                    .seatNumber(seatNumber)
                                    .type(SeatType.valueOf(range.seatType()))
                                    .build()))
            .collect(Collectors.toList());

    List<Seat> savedSeats = seatRepository.saveAll(seats);
    return SeatCreateResponse.from(savedSeats);
  }

  public List<Seat> findSeatsInRange(Long startId, Long endId) {
    return seatRepository.findByIdBetween(startId, endId);
  }
}
