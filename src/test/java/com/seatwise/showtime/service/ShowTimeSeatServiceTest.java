package com.seatwise.showtime.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import com.seatwise.annotation.ServiceTest;
import com.seatwise.common.builder.ShowTestDataBuilder;
import com.seatwise.common.builder.ShowTimeTestDataBuilder;
import com.seatwise.common.builder.VenueTestDataBuilder;
import com.seatwise.core.BusinessException;
import com.seatwise.seat.domain.Seat;
import com.seatwise.seat.domain.SeatGrade;
import com.seatwise.seat.domain.SeatRepository;
import com.seatwise.show.domain.Show;
import com.seatwise.show.domain.ShowType;
import com.seatwise.showtime.domain.ShowTime;
import com.seatwise.showtime.dto.ShowSeatPrice;
import com.seatwise.ticket.TicketService;
import com.seatwise.ticket.domain.Status;
import com.seatwise.ticket.domain.Ticket;
import com.seatwise.ticket.domain.TicketRepository;
import com.seatwise.ticket.dto.ShowSeatCreateRequest;
import com.seatwise.ticket.dto.ShowSeatResponse;
import com.seatwise.venue.domain.Venue;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@ServiceTest
class ShowTimeSeatServiceTest {

  @Autowired private TicketService ticketService;
  @Autowired private SeatRepository seatRepository;
  @Autowired private TicketRepository ticketRepository;
  @Autowired private ShowTimeTestDataBuilder showData;
  @Autowired private ShowTestDataBuilder eventData;
  @Autowired private VenueTestDataBuilder venueData;

  private ShowTime showTime;
  private List<Seat> seats;

  @BeforeEach
  void setUp() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    LocalTime startTime = LocalTime.of(12, 0);
    LocalTime endTime = LocalTime.of(14, 0);

    showTime = showData.withTime(startTime, endTime).withDate(date).build();
    seats = createSeats(5);
  }

  @Test
  void givenValidShowIdAndSeatRequest_whenCreateShowSeat_thenCreatedSuccessfully() {
    // given
    Long startSeatId = seats.get(0).getId();
    Long endSeatId = seats.get(4).getId();
    ShowSeatPrice showSeatPrice = new ShowSeatPrice(startSeatId, endSeatId, 50000);
    ShowSeatCreateRequest request = new ShowSeatCreateRequest(List.of(showSeatPrice));

    // when
    List<Long> showSeatIds = ticketService.createShowSeat(showTime.getId(), request);

    // then
    assertThat(showSeatIds).hasSize(5);
    List<Ticket> tickets = ticketRepository.findAllById(showSeatIds);
    assertThat(tickets)
        .hasSize(5)
        .allSatisfy(
            showSeat -> {
              assertThat(showSeat.getPrice()).isEqualTo(50000);
              assertThat(showSeat.getStatus()).isEqualTo(Status.AVAILABLE);
            });
  }

  @Test
  void givenInvalidShowId_whenCreateShowSeat_thenThrowsException() {
    // given
    Long invalidId = 9999L;
    ShowSeatPrice showSeatPrice =
        new ShowSeatPrice(seats.get(0).getId(), seats.get(4).getId(), 50000);
    ShowSeatCreateRequest request = new ShowSeatCreateRequest(List.of(showSeatPrice));

    // when & then
    assertThatThrownBy(() -> ticketService.createShowSeat(invalidId, request))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void givenValidShowId_whenGetShowSeats_thenReturnsDetailedSeatInfo() {
    // given
    LocalDate date = LocalDate.of(2024, 1, 1);
    LocalTime startTime = LocalTime.of(15, 0);
    Venue venue = venueData.withName("test-venue").withToTalSeat(1000).build();
    Show show =
        eventData
            .withTitle("지킬 앤 하이드")
            .withDescription("test-desc")
            .withType(ShowType.MUSICAL)
            .build();
    showTime =
        showData
            .withEvent(show)
            .withVenue(venue)
            .withDate(date)
            .withTime(startTime, startTime.plusHours(2))
            .build();

    Seat vip1 = new Seat(1, SeatGrade.VIP, venue);
    Seat vip2 = new Seat(2, SeatGrade.VIP, venue);
    Seat rSeat = new Seat(3, SeatGrade.R, venue);
    seatRepository.saveAll(List.of(vip1, vip2, rSeat));

    ticketRepository.saveAll(
        List.of(
            Ticket.createAvailable(showTime, vip1, 40000),
            Ticket.createAvailable(showTime, vip2, 40000),
            Ticket.createAvailable(showTime, rSeat, 20000)));

    // when
    List<ShowSeatResponse> result = ticketService.getShowSeats(showTime.getId());

    // then
    assertThat(result)
        .hasSize(3)
        .extracting("seatNumber", "seatGrade", "status", "isLocked")
        .containsExactlyInAnyOrder(
            tuple(1, SeatGrade.VIP, "예매 가능", true),
            tuple(2, SeatGrade.VIP, "예매 가능", true),
            tuple(3, SeatGrade.R, "예매 가능", true));
  }

  private List<Seat> createSeats(int count) {
    return seatRepository.saveAll(
        IntStream.rangeClosed(1, count)
            .mapToObj(i -> Seat.builder().seatNumber(i).build())
            .toList());
  }
}
