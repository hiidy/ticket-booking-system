package com.seatwise.show.service;

import static org.assertj.core.api.Assertions.*;

import com.seatwise.annotation.ServiceTest;
import com.seatwise.common.exception.ConflictException;
import com.seatwise.common.exception.ErrorCode;
import com.seatwise.common.exception.NotFoundException;
import com.seatwise.event.domain.Event;
import com.seatwise.event.domain.EventType;
import com.seatwise.event.repository.EventRepository;
import com.seatwise.seat.domain.Seat;
import com.seatwise.seat.domain.SeatGrade;
import com.seatwise.seat.repository.SeatRepository;
import com.seatwise.show.domain.Show;
import com.seatwise.show.domain.ShowSeat;
import com.seatwise.show.dto.request.ShowCreateRequest;
import com.seatwise.show.dto.response.ShowResponse;
import com.seatwise.show.repository.ShowRepository;
import com.seatwise.show.repository.ShowSeatRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@ServiceTest
class ShowServiceTest {

  @Autowired private ShowService showService;
  @Autowired private ShowRepository showRepository;
  @Autowired private EventRepository eventRepository;
  @Autowired private SeatRepository seatRepository;
  @Autowired private ShowSeatRepository showSeatRepository;

  private Event event;

  @BeforeEach
  void setUp() {
    event = new Event("지킬 앤 하이드", "테스트 공연", EventType.MUSICAL);
    eventRepository.save(event);
  }

  @Test
  @DisplayName("show를 만들때 시간이 겹치면 예외 반환")
  void createShow_WithOverlappingTime_ThrowsException() {
    // given
    Show existingShow =
        new Show(event, LocalDate.of(2024, 1, 1), LocalTime.of(15, 0), LocalTime.of(17, 0));
    showRepository.save(existingShow);

    ShowCreateRequest request =
        new ShowCreateRequest(
            1L, LocalDate.of(2024, 1, 1), LocalTime.of(16, 0), LocalTime.of(17, 0));

    // when & then
    assertThatThrownBy(() -> showService.createShow(request))
        .isInstanceOf(ConflictException.class)
        .hasMessage(ErrorCode.DUPLICATE_SHOW.getMessage());
  }

  @Test
  @DisplayName("공연 시간별로 상세한 정보를 조회한다.")
  void getShowDetails() {
    // given
    LocalTime startTime = LocalTime.of(15, 0);
    Show show = new Show(event, LocalDate.of(2024, 1, 1), startTime, startTime.plusHours(2));
    showRepository.save(show);

    Seat vipSeat1 = new Seat(1, SeatGrade.VIP, null);
    Seat vipSeat2 = new Seat(2, SeatGrade.VIP, null);
    Seat rSeat = new Seat(3, SeatGrade.R, null);
    seatRepository.saveAll(List.of(vipSeat1, vipSeat2, rSeat));

    ShowSeat showSeat1 = ShowSeat.createAvailable(show, vipSeat1, 40000);
    ShowSeat showSeat2 = ShowSeat.createAvailable(show, vipSeat2, 40000);
    ShowSeat showSeat3 = ShowSeat.createAvailable(show, rSeat, 20000);
    showSeatRepository.saveAll(List.of(showSeat1, showSeat2, showSeat3));

    // when
    ShowResponse response = showService.getShowDetails(show.getId());

    // then
    assertThat(response.startTime()).isEqualTo(startTime);
    assertThat(response.remainingSeats())
        .hasSize(2)
        .extracting("grade", "remainingCount")
        .containsExactlyInAnyOrder(
            tuple(SeatGrade.VIP.getDescription(), 2), tuple(SeatGrade.R.getDescription(), 1));
  }

  @Test
  @DisplayName("공연 상세정보를 불러올 때 없는 공연이면 예외를 던진다")
  void getShowDetailsWithNotExistingShow() {
    LocalTime startTime = LocalTime.of(15, 0);
    Show show = new Show(event, LocalDate.of(2024, 1, 1), startTime, startTime.plusHours(2));
    showRepository.save(show);
    Long showId = 999L;

    assertThatThrownBy(() -> showService.getShowDetails(showId))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  @DisplayName("공연 시간별로 상세한 정보를 불러올 때 이용 가능한 좌석이 없으면 예외를 던진다.")
  void getShowDetailsWithUnavailableSeats() {
    // given
    LocalTime startTime = LocalTime.of(15, 0);
    Show show = new Show(event, LocalDate.of(2024, 1, 1), startTime, startTime.plusHours(2));
    showRepository.save(show);

    Seat vipSeat1 = new Seat(1, SeatGrade.VIP, null);
    Seat vipSeat2 = new Seat(2, SeatGrade.VIP, null);
    Seat rSeat = new Seat(3, SeatGrade.R, null);
    seatRepository.saveAll(List.of(vipSeat1, vipSeat2, rSeat));

    Long showId = show.getId();

    // when & then
    assertThatThrownBy(() -> showService.getShowDetails(showId))
        .isInstanceOf(NotFoundException.class);
  }
}
