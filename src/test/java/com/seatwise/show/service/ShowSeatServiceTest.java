package com.seatwise.show.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import com.seatwise.annotation.ServiceTest;
import com.seatwise.common.builder.EventTestDataBuilder;
import com.seatwise.common.builder.ShowTestDataBuilder;
import com.seatwise.common.builder.VenueTestDataBuilder;
import com.seatwise.common.exception.BusinessException;
import com.seatwise.event.domain.Event;
import com.seatwise.event.domain.EventType;
import com.seatwise.seat.domain.Seat;
import com.seatwise.seat.domain.SeatGrade;
import com.seatwise.seat.repository.SeatRepository;
import com.seatwise.show.domain.Show;
import com.seatwise.show.domain.ShowSeat;
import com.seatwise.show.domain.Status;
import com.seatwise.show.dto.ShowSeatPrice;
import com.seatwise.show.dto.request.ShowSeatCreateRequest;
import com.seatwise.show.dto.response.ShowSeatResponse;
import com.seatwise.show.repository.ShowSeatRepository;
import com.seatwise.venue.domain.Venue;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@ServiceTest
class ShowSeatServiceTest {

  @Autowired private ShowSeatService showSeatService;
  @Autowired private SeatRepository seatRepository;
  @Autowired private ShowSeatRepository showSeatRepository;
  @Autowired private ShowTestDataBuilder showData;
  @Autowired private EventTestDataBuilder eventData;
  @Autowired private VenueTestDataBuilder venueData;

  private Show show;
  private List<Seat> seats;

  @BeforeEach
  void setUp() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    LocalTime startTime = LocalTime.of(12, 0);
    LocalTime endTime = LocalTime.of(14, 0);

    show = showData.withTime(startTime, endTime).withDate(date).build();
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
  void givenInvalidShowId_whenCreateShowSeat_thenThrowsException() {
    // given
    Long invalidId = 9999L;
    ShowSeatPrice showSeatPrice =
        new ShowSeatPrice(seats.get(0).getId(), seats.get(4).getId(), 50000);
    ShowSeatCreateRequest request = new ShowSeatCreateRequest(List.of(showSeatPrice));

    // when & then
    assertThatThrownBy(() -> showSeatService.createShowSeat(invalidId, request))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void givenValidShowId_whenGetShowSeats_thenReturnsDetailedSeatInfo() {
    // given
    LocalDate date = LocalDate.of(2024, 1, 1);
    LocalTime startTime = LocalTime.of(15, 0);
    Venue venue = venueData.withName("test-venue").withToTalSeat(1000).build();
    Event event =
        eventData
            .withTitle("지킬 앤 하이드")
            .withDescription("test-desc")
            .withType(EventType.MUSICAL)
            .build();
    show =
        showData
            .withEvent(event)
            .withVenue(venue)
            .withDate(date)
            .withTime(startTime, startTime.plusHours(2))
            .build();

    Seat vip1 = new Seat(1, SeatGrade.VIP, venue);
    Seat vip2 = new Seat(2, SeatGrade.VIP, venue);
    Seat rSeat = new Seat(3, SeatGrade.R, venue);
    seatRepository.saveAll(List.of(vip1, vip2, rSeat));

    showSeatRepository.saveAll(
        List.of(
            ShowSeat.createAvailable(show, vip1, 40000),
            ShowSeat.createAvailable(show, vip2, 40000),
            ShowSeat.createAvailable(show, rSeat, 20000)));

    // when
    List<ShowSeatResponse> result = showSeatService.getShowSeats(show.getId());

    // then
    assertThat(result)
        .hasSize(3)
        .extracting("seatNumber", "seatGrade", "status", "isLocked")
        .containsExactlyInAnyOrder(
            tuple(1, SeatGrade.VIP, "예매 가능", false),
            tuple(2, SeatGrade.VIP, "예매 가능", false),
            tuple(3, SeatGrade.R, "예매 가능", false));
  }

  private List<Seat> createSeats(int count) {
    return seatRepository.saveAll(
        IntStream.rangeClosed(1, count)
            .mapToObj(i -> Seat.builder().seatNumber(i).build())
            .toList());
  }
}
