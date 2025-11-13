package com.seatwise.ticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import com.seatwise.annotation.ServiceTest;
import com.seatwise.core.BusinessException;
import com.seatwise.show.Show;
import com.seatwise.show.ShowType;
import com.seatwise.showtime.ShowTime;
import com.seatwise.showtime.dto.TicketPrice;
import com.seatwise.support.ShowTestDataBuilder;
import com.seatwise.support.ShowTimeTestDataBuilder;
import com.seatwise.support.VenueTestDataBuilder;
import com.seatwise.ticket.dto.TicketCreateRequest;
import com.seatwise.ticket.dto.TicketResponse;
import com.seatwise.venue.entity.Seat;
import com.seatwise.venue.entity.SeatGrade;
import com.seatwise.venue.entity.SeatRepository;
import com.seatwise.venue.entity.Venue;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@ServiceTest
class TicketServiceTest {

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

    Venue venue = venueData.withName("기본 장소").withToTalSeat(500).build();
    Show show = eventData.withTitle("기본 공연").withType(ShowType.MUSICAL).build();

    showTime =
        showData
            .withEvent(show)
            .withVenue(venue)
            .withTime(startTime, endTime)
            .withDate(date)
            .build();

    seats = createSeats(5);
  }

  @Test
  void shouldCreateTickets_whenValidShowTimeAndSeatRangeProvided() {
    // given
    Long startSeatId = seats.get(0).getId();
    Long endSeatId = seats.get(4).getId();
    TicketPrice ticketPrice = new TicketPrice(startSeatId, endSeatId, 50000);
    TicketCreateRequest request = new TicketCreateRequest(List.of(ticketPrice));

    // when
    List<Long> ticketIds = ticketService.createTickets(showTime.getId(), request);

    // then
    assertThat(ticketIds).hasSize(5);
    List<Ticket> tickets = ticketRepository.findAllById(ticketIds);
    assertThat(tickets)
        .hasSize(5)
        .allSatisfy(
            ticket -> {
              assertThat(ticket.getPrice()).isEqualTo(50000);
              assertThat(ticket.getStatus()).isEqualTo(TicketStatus.AVAILABLE);
            });
  }

  @Test
  void shouldThrowException_whenCreatingTicketsWithInvalidShowTimeId() {
    // given
    Long invalidId = 9999L;
    TicketPrice ticketPrice = new TicketPrice(seats.get(0).getId(), seats.get(4).getId(), 50000);
    TicketCreateRequest request = new TicketCreateRequest(List.of(ticketPrice));

    // when & then
    assertThatThrownBy(() -> ticketService.createTickets(invalidId, request))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void shouldReturnDetailedSeatInfo_whenFetchingTicketsByValidShowTimeId() {
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

    Seat vip1 = Seat.builder().seatNumber(1).venue(venue).grade(SeatGrade.VIP).build();
    Seat vip2 = Seat.builder().seatNumber(2).venue(venue).grade(SeatGrade.VIP).build();
    Seat rSeat = Seat.builder().seatNumber(3).venue(venue).grade(SeatGrade.R).build();
    seatRepository.saveAll(List.of(vip1, vip2, rSeat));

    ticketRepository.saveAll(
        List.of(
            Ticket.createAvailable(showTime, vip1, 40000),
            Ticket.createAvailable(showTime, vip2, 40000),
            Ticket.createAvailable(showTime, rSeat, 20000)));

    // when
    List<TicketResponse> result = ticketService.getTickets(showTime.getId());

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
