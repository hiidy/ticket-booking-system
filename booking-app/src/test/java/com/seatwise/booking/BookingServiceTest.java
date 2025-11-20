package com.seatwise.booking;

import static org.assertj.core.api.Assertions.*;

import com.seatwise.annotation.ServiceTest;
import com.seatwise.booking.exception.BookingException;
import com.seatwise.core.BaseCode;
import com.seatwise.member.Member;
import com.seatwise.member.MemberRepository;
import com.seatwise.show.entity.Show;
import com.seatwise.show.entity.ShowType;
import com.seatwise.show.service.ShowBookingService;
import com.seatwise.support.ShowTestDataBuilder;
import com.seatwise.support.VenueTestDataBuilder;
import com.seatwise.show.entity.Ticket;
import com.seatwise.show.repository.TicketRepository;
import com.seatwise.show.entity.TicketStatus;
import com.seatwise.venue.entity.Seat;
import com.seatwise.venue.entity.SeatGrade;
import com.seatwise.venue.entity.SeatRepository;
import com.seatwise.venue.entity.Venue;
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

  @Autowired private ShowBookingService showBookingService;
  @Autowired private TicketRepository ticketRepository;
  @Autowired private SeatRepository seatRepository;
  @Autowired private MemberRepository memberRepository;
  @Autowired private ShowTestDataBuilder showData;
  @Autowired private VenueTestDataBuilder venueData;

  @BeforeEach
  void setUp() {
    LocalDate date = LocalDate.of(2025, 1, 1);
    LocalTime startTime = LocalTime.of(18, 0);
    LocalTime endTime = LocalTime.of(20, 0);

    Venue venue = venueData.withName("테스트 장소").withToTalSeat(100).build();
    Show show = showData
        .withTitle("테스트 공연")
        .withType(ShowType.CONCERT)
        .withVenue(venue)
        .withDate(date)
        .withTime(startTime, endTime)
        .build();

    Seat seat = Seat.builder().seatNumber(1).grade(SeatGrade.A).build();
    seatRepository.save(seat);
    ticketRepository.save(Ticket.createAvailable(show, seat, 40000));

    member = new Member("테스트유저", "abcd@gmail.com", "1234");
    memberRepository.save(member);
  }

  @Test
  void shouldCreateBookingAndLockTicket() {
    // given
    UUID requestId = UUID.randomUUID();
    Long ticketId = ticketRepository.findAll().get(0).getId();

    // when
    String bookingId = showBookingService.create(requestId, member.getId(), List.of(ticketId));

    // then
    TicketStatus status = ticketRepository.findById(ticketId).orElseThrow().getStatus();
    assertThat(status).isEqualTo(TicketStatus.PAYMENT_PENDING);
  }

  @Test
  void shouldThrow_whenTicketDoesNotExist() {
    // given
    UUID requestId = UUID.randomUUID();
    List<Long> invalidTicketIds = List.of(999L);
    Long memberId = member.getId();

    // when & then
    assertThatThrownBy(() -> showBookingService.create(requestId, memberId, invalidTicketIds))
        .isInstanceOf(BookingException.class)
        .hasFieldOrPropertyWithValue("baseCode", BaseCode.SEAT_NOT_AVAILABLE);
  }

  @Test
  void shouldThrow_whenDuplicateIdempotencyKeyUsed() {
    // given
    UUID duplicatedId = UUID.randomUUID();
    List<Long> ticketIds = List.of(ticketRepository.findAll().get(0).getId());
    Long memberId = member.getId();

    showBookingService.create(duplicatedId, memberId, ticketIds);

    // when & then
    assertThatThrownBy(() -> showBookingService.create(duplicatedId, memberId, ticketIds))
        .isInstanceOf(BookingException.class)
        .hasFieldOrPropertyWithValue("baseCode", BaseCode.DUPLICATE_IDEMPOTENCY_KEY);
  }

  @Test
  void shouldThrow_whenTicketAlreadyLockedByOtherMember() {
    // given
    Member other = new Member("new member", "test@gmail.com", "test");
    Long otherMemberId = memberRepository.save(other).getId();
    List<Long> ticketIds = List.of(ticketRepository.findAll().get(0).getId());

    UUID firstRequestId = UUID.randomUUID();
    UUID secondRequestId = UUID.randomUUID();

    showBookingService.create(firstRequestId, member.getId(), ticketIds);

    // when & then
    assertThatThrownBy(
            () -> showBookingService.create(secondRequestId, otherMemberId, ticketIds))
        .isInstanceOf(BookingException.class)
        .hasFieldOrPropertyWithValue("baseCode", BaseCode.SEAT_NOT_AVAILABLE);
  }
}
