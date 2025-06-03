package com.seatwise.showtime.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.seatwise.seat.domain.Seat;
import com.seatwise.seat.domain.SeatGrade;
import com.seatwise.seat.domain.SeatRepository;
import com.seatwise.ticket.domain.Ticket;
import com.seatwise.ticket.domain.TicketRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ShowTimeSeatRepositoryTest {

  @Autowired TicketRepository ticketRepository;
  @Autowired SeatRepository seatRepository;
  @Autowired ShowTimeRepository showTimeRepository;

  @Test
  @DisplayName("공연 ID로 ShowSeat를 조회한다.")
  void findAllByShowId() {
    // given
    ShowTime showTime =
        new ShowTime(
            null, null, LocalDate.of(2025, 1, 1), LocalTime.of(14, 0), LocalTime.of(15, 0));
    showTimeRepository.save(showTime);
    Seat seat1 = new Seat(1, SeatGrade.VIP, null);
    Seat seat2 = new Seat(2, SeatGrade.VIP, null);
    seatRepository.saveAll(List.of(seat1, seat2));

    Ticket ticket = Ticket.createAvailable(showTime, seat1, 40000);
    Ticket ticket1 = Ticket.createAvailable(showTime, seat2, 40000);
    ticketRepository.saveAll(List.of(ticket, ticket1));

    // when
    List<Ticket> tickets = ticketRepository.findAllByShowTimeId(showTime.getId());

    // then
    assertThat(tickets).hasSize(2);
    assertThat(tickets.get(0).getSeat().getSeatNumber()).isEqualTo(1);
    assertThat(tickets.get(1).getSeat().getSeatNumber()).isEqualTo(2);
  }

  @Test
  void findAllShowSeatsByShowId() {
    // given
    ShowTime showTime =
        new ShowTime(
            null, null, LocalDate.of(2025, 1, 1), LocalTime.of(14, 0), LocalTime.of(15, 0));
    showTimeRepository.save(showTime);
    Seat seat1 = new Seat(1, SeatGrade.VIP, null);
    Seat seat2 = new Seat(2, SeatGrade.VIP, null);
    Seat seat3 = new Seat(3, SeatGrade.VIP, null);
    seatRepository.saveAll(List.of(seat1, seat2, seat3));

    Ticket ticket = Ticket.createAvailable(showTime, seat1, 40000);
    Ticket ticket1 = Ticket.createAvailable(showTime, seat2, 40000);
    ticketRepository.saveAll(List.of(ticket, ticket1));

    // when
    List<Ticket> tickets = ticketRepository.findAllByShowTimeId(showTime.getId());

    // then
    assertThat(tickets).hasSize(2);
    assertThat(tickets.get(0).getSeat().getSeatNumber()).isEqualTo(1);
    assertThat(tickets.get(1).getSeat().getSeatNumber()).isEqualTo(2);
  }
}
