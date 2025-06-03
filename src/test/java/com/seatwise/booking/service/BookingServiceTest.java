package com.seatwise.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.seatwise.annotation.ServiceTest;
import com.seatwise.booking.BookingService;
import com.seatwise.booking.exception.BookingException;
import com.seatwise.common.builder.ShowTimeTestDataBuilder;
import com.seatwise.core.ErrorCode;
import com.seatwise.member.Member;
import com.seatwise.member.MemberRepository;
import com.seatwise.seat.domain.Seat;
import com.seatwise.seat.domain.SeatGrade;
import com.seatwise.seat.domain.SeatRepository;
import com.seatwise.showtime.domain.ShowTime;
import com.seatwise.ticket.domain.Status;
import com.seatwise.ticket.domain.Ticket;
import com.seatwise.ticket.domain.TicketRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@ServiceTest
class BookingServiceTest {

  Member member;
  @Autowired private BookingService bookingService;
  @Autowired private TicketRepository ticketRepository;
  @Autowired private SeatRepository seatRepository;
  @Autowired private MemberRepository memberRepository;
  @Autowired private ShowTimeTestDataBuilder showTimeTestDataBuilder;

  @BeforeEach
  void setUp() {
    LocalDate date = LocalDate.of(2025, 1, 1);
    LocalTime startTime = LocalTime.of(18, 0);
    LocalTime endTime = LocalTime.of(20, 0);

    ShowTime showTime = showTimeTestDataBuilder.withTime(startTime, endTime).withDate(date).build();

    Seat seat = Seat.builder().seatNumber(1).grade(SeatGrade.A).build();
    seatRepository.save(seat);

    ticketRepository.save(Ticket.createAvailable(showTime, seat, 40000));

    member = new Member("테스트유저", "abcd@gmail.com", "1234");
    memberRepository.save(member);
  }

  @Test
  void shouldCreateBookingSuccessfully() {
    // given
    UUID requestId = UUID.randomUUID();
    Long showSeatId = ticketRepository.findAll().get(0).getId();

    // when
    Long bookingId = bookingService.createBooking(requestId, member.getId(), List.of(showSeatId));

    // then
    assertThat(bookingId).isPositive();
    Status status = ticketRepository.findById(showSeatId).orElseThrow().getStatus();
    assertThat(status).isEqualTo(Status.PAYMENT_PENDING);
  }

  @Test
  void shouldThrowExceptionWhenSeatNotFound() {
    // given
    Long memberId = member.getId();
    UUID requestId = UUID.randomUUID();
    List<Long> invalidSeatId = List.of(999L);

    // When & Then
    assertThatThrownBy(() -> bookingService.createBooking(requestId, memberId, invalidSeatId))
        .isInstanceOf(BookingException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEAT_NOT_AVAILABLE);
  }

  @Test
  void shouldThrowWhenDuplicateRequestId() {
    // given
    Long memberId = member.getId();
    UUID duplicatedId = UUID.randomUUID();
    List<Long> ticketIds = List.of(ticketRepository.findAll().get(0).getId());

    bookingService.createBooking(duplicatedId, memberId, ticketIds);

    // when & then
    assertThatThrownBy(() -> bookingService.createBooking(duplicatedId, memberId, ticketIds))
        .isInstanceOf(BookingException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_IDEMPOTENCY_KEY);
  }

  @Test
  void canNotBeAssignedWhenTicketIsLocked() {
    // given
    Long newMemberId =
        memberRepository.save(new Member("new member", "test@gmail.com", "test")).getId();
    List<Long> ticketIds = List.of(ticketRepository.findAll().get(0).getId());
    UUID firstRequestId = UUID.randomUUID();
    UUID secondRequestId = UUID.randomUUID();

    bookingService.createBooking(firstRequestId, member.getId(), ticketIds);

    // when & then
    assertThatThrownBy(() -> bookingService.createBooking(secondRequestId, newMemberId, ticketIds))
        .isInstanceOf(BookingException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEAT_NOT_AVAILABLE);
  }
}
