package com.seatwise.show.service;

import com.seatwise.seat.service.SeatService;
import com.seatwise.show.domain.Show;
import com.seatwise.show.domain.ShowSeat;
import com.seatwise.show.domain.Status;
import com.seatwise.show.dto.ShowSeatCreateDto;
import com.seatwise.show.dto.response.ShowSeatCreateResponse;
import com.seatwise.show.repository.ShowSeatRepository;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShowSeatService {

  private final ShowSeatRepository showSeatRepository;
  private final ShowService showService;
  private final SeatService seatService;

  public ShowSeatCreateResponse createShowSeat(ShowSeatCreateDto createDto) {

    Show show = showService.findById(createDto.showId());

    List<ShowSeat> showSeats =
        createDto.showSeatPrices().stream()
            .map(
                seatPrice ->
                    seatService
                        .findSeatsInRange(seatPrice.startSeatId(), seatPrice.endSeatId())
                        .stream()
                        .map(
                            seat ->
                                ShowSeat.builder()
                                    .show(show)
                                    .seat(seat)
                                    .price(seatPrice.price())
                                    .status(Status.AVAILABLE)
                                    .build())
                        .collect(Collectors.toList()))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

    List<ShowSeat> savedShowSeats = showSeatRepository.saveAll(showSeats);
    return ShowSeatCreateResponse.from(savedShowSeats);
  }
}
