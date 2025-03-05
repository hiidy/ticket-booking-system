package com.seatwise.show.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.seatwise.common.exception.NotFoundException;
import com.seatwise.seat.domain.Seat;
import com.seatwise.seat.repository.SeatRepository;
import com.seatwise.show.domain.Show;
import com.seatwise.show.domain.ShowSeat;
import com.seatwise.show.domain.Status;
import com.seatwise.show.dto.ShowSeatPrice;
import com.seatwise.show.dto.request.ShowSeatCreateRequest;
import com.seatwise.show.repository.ShowRepository;
import com.seatwise.show.repository.ShowSeatRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
class ShowSeatServiceTest {

  @Autowired private ShowSeatService showSeatService;

  @Autowired private ShowRepository showRepository;

  @Autowired private SeatRepository seatRepository;

  @Autowired private ShowSeatRepository showSeatRepository;

  private Show show;
  private List<Seat> seats;

  @BeforeEach
  void setUp() {
    show = new Show(null, LocalDate.of(2024, 1, 1), LocalTime.of(12, 0), LocalTime.of(14, 0));

    showRepository.save(show);

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
  @DisplayName("ShowSeat 성공적으로 생성")
  void createShow_WithValidRequest_Success() {
    // given
    Long startSeatId = seats.get(0).getId();
    Long endSeatId = seats.get(4).getId();
    ShowSeatPrice showSeatPrice = new ShowSeatPrice(startSeatId, endSeatId, 50000);
    ShowSeatCreateRequest request = new ShowSeatCreateRequest(List.of(showSeatPrice));

    // when
    List<Long> showSeatIds = showSeatService.createShowSeat(show.getId(), request);

    // then
    assertThat(showSeatIds).hasSize(5);
    List<ShowSeat> showSeats = showSeatRepository.findAllByIds(showSeatIds);
    assertThat(showSeats)
        .hasSize(5)
        .allSatisfy(
            showSeat -> {
              assertThat(showSeat.getPrice()).isEqualTo(50000);
              assertThat(showSeat.getStatus()).isEqualTo(Status.AVAILABLE);
            });
  }

  @Test
  @DisplayName("존재하지 않은 Show로 ShowSeat를 생성하면 예외 발생")
  void createShow_WithInvalidShowId_ThrowsException() {
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
