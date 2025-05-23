package com.seatwise.show.service;

import com.seatwise.common.exception.BusinessException;
import com.seatwise.common.exception.ErrorCode;
import com.seatwise.seat.repository.SeatRepository;
import com.seatwise.show.domain.Show;
import com.seatwise.show.domain.ShowSeat;
import com.seatwise.show.dto.request.ShowSeatCreateRequest;
import com.seatwise.show.dto.response.SeatAvailabilityResponse;
import com.seatwise.show.dto.response.ShowSeatResponse;
import com.seatwise.show.repository.ShowRepository;
import com.seatwise.show.repository.ShowSeatRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShowSeatService {

  private final ShowSeatRepository showSeatRepository;
  private final ShowRepository showRepository;
  private final SeatRepository seatRepository;

  public List<Long> createShowSeat(Long showId, ShowSeatCreateRequest request) {

    Show show =
        showRepository
            .findById(showId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SHOW_NOT_FOUND));

    List<ShowSeat> showSeats =
        request.showSeatPrices().stream()
            .map(
                seatPrice ->
                    seatRepository
                        .findByIdBetween(seatPrice.startSeatId(), seatPrice.endSeatId())
                        .stream()
                        .map(seat -> ShowSeat.createAvailable(show, seat, seatPrice.price()))
                        .toList())
            .flatMap(Collection::stream)
            .toList();

    List<ShowSeat> savedShowSeats = showSeatRepository.saveAll(showSeats);
    return savedShowSeats.stream().map(ShowSeat::getId).toList();
  }

  public List<ShowSeatResponse> getShowSeats(Long showId) {
    List<ShowSeat> showSeats = showSeatRepository.findAllByShowId(showId);
    LocalDateTime requestTime = LocalDateTime.now();
    if (showSeats.isEmpty()) {
      throw new BusinessException(ErrorCode.SHOW_SEAT_NOT_FOUND);
    }
    return showSeats.stream()
        .map(showSeat -> ShowSeatResponse.from(showSeat, requestTime))
        .toList();
  }

  public List<SeatAvailabilityResponse> getAvailableSeatsForShow(Long showId) {
    return showSeatRepository.findSeatAvailabilityByShowId(showId);
  }
}
