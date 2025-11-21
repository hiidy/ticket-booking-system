package com.seatwise.show.ticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import com.seatwise.annotation.ServiceTest;
import com.seatwise.core.exception.BusinessException;
import com.seatwise.show.dto.TicketPrice;
import com.seatwise.show.dto.request.TicketCreateRequest;
import com.seatwise.show.dto.response.TicketResponse;
import com.seatwise.show.entity.Show;
import com.seatwise.show.entity.ShowType;
import com.seatwise.show.entity.Ticket;
import com.seatwise.show.entity.TicketStatus;
import com.seatwise.show.repository.TicketRepository;
import com.seatwise.show.service.TicketService;
import com.seatwise.support.ShowTestDataBuilder;
import com.seatwise.support.VenueTestDataBuilder;
import com.seatwise.venue.entity.Seat;
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
  @Autowired private ShowTestDataBuilder showData;
  @Autowired private VenueTestDataBuilder venueData;

  private Show show;
  private List<Seat> seats;

  @BeforeEach
  void setUp() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    LocalTime startTime = LocalTime.of(12, 0);
    LocalTime endTime = LocalTime.of(14, 0);

    Venue venue = venueData.withName("기본 장소").withToTalSeat(500).build();

    show =
        showData
            .withTitle("기본 공연")
            .withType(ShowType.MUSICAL)
            .withVenue(venue)
            .withDate(date)
            .withTime(startTime, endTime)
            .build();

    seats = createSeats(5);
  }

  @Test
  void shouldCreateTickets_whenValidShowAndSeatRangeProvided() {
    // given
    Long startSeatId = seats.get(0).getId();
    Long endSeatId = seats.get(4).getId();
    TicketPrice ticketPrice = new TicketPrice(startSeatId, endSeatId, 50000);
    TicketCreateRequest request = new TicketCreateRequest(List.of(ticketPrice));

    // when
    List<Long> ticketIds = ticketService.createTickets(show.getId(), request);

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
  void shouldThrowException_whenCreatingTicketsWithInvalidShowId() {
    // given
    Long invalidId = 9999L;
    TicketPrice ticketPrice = new TicketPrice(seats.get(0).getId(), seats.get(4).getId(), 50000);
    TicketCreateRequest request = new TicketCreateRequest(List.of(ticketPrice));

    // when & then
    assertThatThrownBy(() -> ticketService.createTickets(invalidId, request))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void shouldReturnDetailedSeatInfo_whenFetchingTicketsByValidShowId() {
    // given
    LocalDate date = LocalDate.of(2024, 1, 1);
    LocalTime startTime = LocalTime.of(15, 0);
    Venue venue = venueData.withName("test-venue").withToTalSeat(1000).build();
    Show testShow =
        showData
            .withTitle("지킬 앤 하이드")
            .withDescription("test-desc")
            .withType(ShowType.MUSICAL)
            .withVenue(venue)
            .withDate(date)
            .withTime(startTime, startTime.plusHours(2))
            .build();

    Seat vip1 = new Seat("VIP", "1", venue);
    Seat vip2 = new Seat("VIP", "2", venue);
    Seat rSeat = new Seat("R", "3", venue);
    seatRepository.saveAll(List.of(vip1, vip2, rSeat));

    ticketRepository.saveAll(
        List.of(
            Ticket.createAvailable(testShow, vip1, 1L, 40000),
            Ticket.createAvailable(testShow, vip2, 1L, 40000),
            Ticket.createAvailable(testShow, rSeat, 2L, 20000)));

    // when
    List<TicketResponse> result = ticketService.getTickets(testShow.getId());

    // then
    assertThat(result)
        .hasSize(3)
        .extracting("seatNumber", "status", "isLocked")
        .containsExactlyInAnyOrder(
            tuple("VIP-1", "예매 가능", true),
            tuple("VIP-2", "예매 가능", true),
            tuple("R-3", "예매 가능", true));
  }

  private List<Seat> createSeats(int count) {
    Venue venue = venueData.withName("Test Venue").withToTalSeat(count).build();
    return seatRepository.saveAll(
        IntStream.rangeClosed(1, count)
            .mapToObj(
                i ->
                    new Seat(
                        String.valueOf((char) ('A' + (i - 1) / 10)),
                        String.valueOf((i - 1) % 10 + 1),
                        venue))
            .toList());
  }
}
