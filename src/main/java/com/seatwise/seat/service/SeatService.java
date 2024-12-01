package com.seatwise.seat.service;

import com.seatwise.seat.domain.Seat;
import com.seatwise.seat.dto.SeatCreateDto;
import com.seatwise.seat.dto.SeatCreateRequest;
import com.seatwise.seat.dto.SeatCreateResponse;
import com.seatwise.seat.dto.SeatsCreateResponse;
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

  public SeatCreateResponse createSeat(SeatCreateRequest seatCreateRequest) {
    Seat saveSeat = seatRepository.save(seatCreateRequest.toEntity());
    return SeatCreateResponse.from(saveSeat);
  }

  public SeatsCreateResponse createSeats(SeatCreateDto createDto) {
    Venue venue = venueService.findById(createDto.venueId());

    List<Seat> seats =
        createDto.seatTypeRanges().stream()
            .flatMap(
                range ->
                    IntStream.rangeClosed(range.startNumber(), range.endNumber())
                        .mapToObj(
                            seatNumber ->
                                Seat.builder()
                                    .venue(venue)
                                    .seatNumber(seatNumber)
                                    .type(range.seatType())
                                    .build()))
            .collect(Collectors.toList());

    List<Seat> savedSeats = seatRepository.saveAll(seats);
    return SeatsCreateResponse.from(savedSeats);
  }
}
