package com.seatwise.show.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.seatwise.annotation.ServiceTest;
import com.seatwise.common.builder.ShowTestDataBuilder;
import com.seatwise.common.exception.NotFoundException;
import com.seatwise.seat.domain.Seat;
import com.seatwise.seat.repository.SeatRepository;
import com.seatwise.show.domain.Show;
import com.seatwise.show.domain.ShowSeat;
import com.seatwise.show.domain.Status;
import com.seatwise.show.dto.ShowSeatPrice;
import com.seatwise.show.dto.request.ShowSeatCreateRequest;
import com.seatwise.show.repository.ShowSeatRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@ServiceTest
class ShowSeatServiceTest {

  @Autowired private ShowSeatService showSeatService;
  @Autowired private SeatRepository seatRepository;
  @Autowired private ShowSeatRepository showSeatRepository;
  @Autowired private ShowTestDataBuilder showData;

  private Show show;
  private List<Seat> seats;

  @BeforeEach
  void setUp() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    LocalTime startTime = LocalTime.of(12, 0);
    LocalTime endTime = LocalTime.of(14, 0);

    show = showData.withTime(startTime, endTime).withDate(date).build();
    seats =
        seatRepository.saveAll(
            List.of(
                Seat.builder().seatNumber(1).build(),
                Seat.builder().seatNumber(2).build(),
                Seat.builder().seatNumber(3).build(),
                Seat.builder().seatNumber(4).build(),
                Seat.builder().seatNumber(5).build()));
  }

  @Test
  void givenValidShowIdAndSeatRequest_whenCreateShowSeat_thenCreatedSuccessfully() {
    // given
    Long startSeatId = seats.get(0).getId();
    Long endSeatId = seats.get(4).getId();
    ShowSeatPrice showSeatPrice = new ShowSeatPrice(startSeatId, endSeatId, 50000);
    ShowSeatCreateRequest request = new ShowSeatCreateRequest(List.of(showSeatPrice));

    // when
    List<Long> showSeatIds = showSeatService.createShowSeat(show.getId(), request);

    // then
    assertThat(showSeatIds).hasSize(5);
    List<ShowSeat> showSeats = showSeatRepository.findAllById(showSeatIds);
    assertThat(showSeats)
        .hasSize(5)
        .allSatisfy(
            showSeat -> {
              assertThat(showSeat.getPrice()).isEqualTo(50000);
              assertThat(showSeat.getStatus()).isEqualTo(Status.AVAILABLE);
            });
  }

  @Test
  void givenInvalidShowId_whenCreateShowSeat_thenThrowsNotFoundException() {
    // given
    Long invalidId = 9999L;
    ShowSeatPrice showSeatPrice =
        new ShowSeatPrice(seats.get(0).getId(), seats.get(4).getId(), 50000);
    ShowSeatCreateRequest request = new ShowSeatCreateRequest(List.of(showSeatPrice));

    // when & then
    assertThatThrownBy(() -> showSeatService.createShowSeat(invalidId, request))
        .isInstanceOf(NotFoundException.class);
  }
}
