package com.seatwise.venue;

import com.seatwise.core.BaseCode;
import com.seatwise.core.exception.BusinessException;
import com.seatwise.venue.dto.request.SeatCreateRequest;
import com.seatwise.venue.dto.request.SeatRangeRequest;
import com.seatwise.venue.dto.response.SeatCreateResponse;
import com.seatwise.venue.entity.Seat;
import com.seatwise.venue.entity.SeatRepository;
import com.seatwise.venue.entity.Venue;
import com.seatwise.venue.entity.VenueRepository;
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

    // 기존 좌석 확인
    List<Seat> existingSeats = seatRepository.findByVenueId(request.venueId());

    // 새로운 좌석 생성
    List<Seat> newSeats = createSeatsFromRanges(request.seatRanges(), venue);

    // 중복 좌석 확인
    validateSeatDuplicates(existingSeats, newSeats);

    List<Seat> savedSeats = seatRepository.saveAll(newSeats);
    return SeatCreateResponse.from(savedSeats);
  }

  private List<Seat> createSeatsFromRanges(List<SeatRangeRequest> seatRanges, Venue venue) {
    return seatRanges.stream()
        .flatMap(
            range ->
                IntStream.rangeClosed(range.startCol(), range.endCol())
                    .mapToObj(col -> new Seat(range.rowName(), String.valueOf(col), venue)))
        .toList();
  }

  private void validateSeatDuplicates(List<Seat> existingSeats, List<Seat> newSeats) {
    for (Seat newSeat : newSeats) {
      boolean hasDuplicate =
          existingSeats.stream()
              .anyMatch(
                  existing ->
                      existing.getRowName().equals(newSeat.getRowName())
                          && existing.getColName().equals(newSeat.getColName()));

      if (hasDuplicate) {
        throw new BusinessException(BaseCode.DUPLICATE_SEAT_NUMBER);
      }
    }
  }
}
