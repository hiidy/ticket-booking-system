package com.seatwise.venue;

import com.seatwise.core.exception.BusinessException;
import com.seatwise.core.BaseCode;
import com.seatwise.venue.entity.Seat;
import com.seatwise.venue.entity.SeatGrade;
import com.seatwise.venue.entity.SeatRepository;
import com.seatwise.venue.entity.Venue;
import com.seatwise.venue.entity.VenueRepository;
import com.seatwise.venue.dto.request.SeatCreateRequest;
import com.seatwise.venue.dto.request.SeatGradeRangeRequest;
import com.seatwise.venue.dto.response.SeatCreateResponse;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SeatService {

  private final SeatRepository seatRepository;
  private final VenueRepository venueRepository;

  @Transactional
  public SeatCreateResponse createSeat(SeatCreateRequest request) {
    Venue venue =
        venueRepository
            .findById(request.venueId())
            .orElseThrow(() -> new BusinessException(BaseCode.VENUE_NOT_FOUND));

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

  private List<Integer> extractSeatNumbers(List<SeatGradeRangeRequest> request) {
    return request.stream()
        .flatMap(range -> IntStream.rangeClosed(range.startNumber(), range.endNumber()).boxed())
        .toList();
  }

  private void validateNewSeatNumbers(List<Integer> existing, List<Integer> incoming) {
    boolean hasDuplicate = incoming.stream().anyMatch(existing::contains);

    if (hasDuplicate) {
      throw new BusinessException(BaseCode.DUPLICATE_SEAT_NUMBER);
    }
  }
}
