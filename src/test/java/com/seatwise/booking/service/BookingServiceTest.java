package com.seatwise.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.seatwise.common.exception.ErrorCode;
import com.seatwise.common.exception.NotFoundException;
import com.seatwise.seat.domain.Seat;
import com.seatwise.seat.domain.SeatGrade;
import com.seatwise.seat.repository.SeatRepository;
import com.seatwise.show.domain.Show;
import com.seatwise.show.domain.ShowSeat;
import com.seatwise.show.domain.Status;
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
class BookingServiceTest {

  @Autowired private BookingService bookingService;

  @Autowired private ShowSeatRepository showSeatRepository;

  @Autowired private ShowRepository showRepository;

  @Autowired private SeatRepository seatRepository;

  @BeforeEach
  void setUp() {
    Show show = new Show(null, LocalDate.of(2025, 1, 1), LocalTime.of(18, 0), LocalTime.of(20, 0));

    showRepository.save(show);

    Seat seat = Seat.builder().seatNumber(1).grade(SeatGrade.A).build();
    seatRepository.save(seat);

    ShowSeat showSeat = ShowSeat.createAvailable(show, seat, 40000);
    showSeatRepository.save(showSeat);
  }

  @Test
  @DisplayName("좌석 예약 성공")
  void testCreateBookingSuccessfully() {
    // When
    Long bookingId = bookingService.createBooking(List.of(1L));

    // then
    assertThat(bookingId).isNotNull();
    ShowSeat showSeat = showSeatRepository.findByShowIdAndSeatId(1L, 1L).orElseThrow();
    assertThat(showSeat.getStatus()).isEqualTo(Status.RESERVED);
  }

  @Test
  @DisplayName("존재하지 않는 좌석으로 예약을 할 수 없다.")
  void testCreateBookingWhenInvalidShowSeat() {
    // When & Then
    assertThatThrownBy(() -> bookingService.createBooking(List.of(999L)))
        .isInstanceOf(NotFoundException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SHOW_SEAT_NOT_FOUND);
  }
}
