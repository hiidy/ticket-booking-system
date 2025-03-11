package com.seatwise.show.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.seatwise.seat.domain.Seat;
import com.seatwise.seat.domain.SeatGrade;
import com.seatwise.seat.repository.SeatRepository;
import com.seatwise.show.repository.ShowRepository;
import com.seatwise.show.repository.ShowSeatRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ShowSeatRepositoryTest {

  @Autowired ShowSeatRepository showSeatRepository;
  @Autowired SeatRepository seatRepository;
  @Autowired ShowRepository showRepository;

  @Test
  @DisplayName("공연 ID로 ShowSeat를 조회한다.")
  void findByShowId() {
    // given
    Show show = new Show(null, LocalDate.of(2025, 1, 1), LocalTime.of(14, 0), LocalTime.of(15, 0));
    showRepository.save(show);
    Seat seat1 = new Seat(1, SeatGrade.VIP, null);
    Seat seat2 = new Seat(2, SeatGrade.VIP, null);
    seatRepository.saveAll(List.of(seat1, seat2));

    ShowSeat showSeat = ShowSeat.createAvailable(show, seat1, 40000);
    ShowSeat showSeat1 = ShowSeat.createAvailable(show, seat2, 40000);
    showSeatRepository.saveAll(List.of(showSeat, showSeat1));

    // when
    List<ShowSeat> showSeats = showSeatRepository.findByShowId(show.getId());

    // then
    assertThat(showSeats).hasSize(2);
    assertThat(showSeats.get(0).getSeat().getSeatNumber()).isEqualTo(1);
    assertThat(showSeats.get(1).getSeat().getSeatNumber()).isEqualTo(2);
  }
}
