package com.seatwise.show.service;

import com.seatwise.seat.repository.SeatRepository;
import com.seatwise.show.domain.Show;
import com.seatwise.show.domain.ShowSeat;
import com.seatwise.show.domain.Status;
import com.seatwise.show.dto.ShowSeatCreateDto;
import com.seatwise.show.dto.response.ShowSeatCreateResponse;
import com.seatwise.show.repository.ShowSeatRepository;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShowSeatService {

  private final ShowSeatRepository showSeatRepository;
  private final ShowService showService;
  private final SeatRepository seatRepository;

  public ShowSeatCreateResponse createShowSeat(ShowSeatCreateDto createDto) {

    Show show = showService.findById(createDto.showId());

    List<ShowSeat> showSeats =
        createDto.showSeatPrices().stream()
            .map(
                seatPrice ->
                    seatRepository
                        .findByIdBetween(seatPrice.startSeatId(), seatPrice.endSeatId())
                        .stream()
                        .map(
                            seat ->
                                ShowSeat.builder()
                                    .show(show)
                                    .seat(seat)
                                    .price(seatPrice.price())
                                    .status(Status.AVAILABLE)
                                    .build())
                        .toList())
            .flatMap(Collection::stream)
            .toList();

    List<ShowSeat> savedShowSeats = showSeatRepository.saveAll(showSeats);
    return ShowSeatCreateResponse.from(savedShowSeats);
  }
}
