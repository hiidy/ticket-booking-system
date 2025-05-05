package com.seatwise.seat.service;

import com.seatwise.common.exception.BusinessException;
import com.seatwise.common.exception.ErrorCode;
import com.seatwise.seat.domain.Seat;
import com.seatwise.seat.domain.SeatGrade;
import com.seatwise.seat.dto.request.SeatCreateRequest;
import com.seatwise.seat.dto.request.SeatGradeRangeRequest;
import com.seatwise.seat.dto.response.SeatCreateResponse;
import com.seatwise.seat.repository.SeatRepository;
import com.seatwise.venue.domain.Venue;
import com.seatwise.venue.repository.VenueRepository;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SeatService {

  private final SeatRepository seatRepository;
  private final VenueRepository venueRepository;

  public SeatCreateResponse createSeat(SeatCreateRequest request) {
    Venue venue =
        venueRepository
            .findById(request.venueId())
            .orElseThrow(() -> new BusinessException(ErrorCode.VENUE_NOT_FOUND));

    List<Integer> existsSeatNumbers =
        seatRepository.findByVenueId(request.venueId()).stream().map(Seat::getSeatNumber).toList();
    List<Integer> newSeatNumbers = extractSeatNumbers(request.seatTypeRanges());
    validateNewSeatNumbers(existsSeatNumbers, newSeatNumbers);

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
                                    .grade(SeatGrade.valueOf(range.grade()))
                                    .build()))
            .toList();

    List<Seat> savedSeats = seatRepository.saveAll(seats);
    return SeatCreateResponse.from(savedSeats);
  }

  public List<Integer> extractSeatNumbers(List<SeatGradeRangeRequest> request) {
    return request.stream()
        .flatMap(range -> IntStream.rangeClosed(range.startNumber(), range.endNumber()).boxed())
        .toList();
  }

  private void validateNewSeatNumbers(List<Integer> oldNumbers, List<Integer> newNumbers) {
    boolean hasDuplicate = newNumbers.stream().anyMatch(oldNumbers::contains);

    if (hasDuplicate) {
      throw new BusinessException(ErrorCode.DUPLICATE_SEAT_NUMBER);
    }
  }
}
