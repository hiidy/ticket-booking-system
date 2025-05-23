package com.seatwise.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.seatwise.annotation.ServiceTest;
import com.seatwise.common.builder.ShowTestDataBuilder;
import com.seatwise.common.exception.BookingException;
import com.seatwise.common.exception.ErrorCode;
import com.seatwise.member.domain.Member;
import com.seatwise.member.repository.MemberRepository;
import com.seatwise.seat.domain.Seat;
import com.seatwise.seat.domain.SeatGrade;
import com.seatwise.seat.repository.SeatRepository;
import com.seatwise.show.domain.Show;
import com.seatwise.show.domain.ShowSeat;
import com.seatwise.show.domain.Status;
import com.seatwise.show.repository.ShowSeatRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@ServiceTest
class BookingServiceTest {

  @Autowired private BookingService bookingService;
  @Autowired private ShowSeatRepository showSeatRepository;
  @Autowired private SeatRepository seatRepository;
  @Autowired private MemberRepository memberRepository;
  @Autowired private ShowTestDataBuilder showTestDataBuilder;

  Member member;
  UUID requestId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    LocalDate date = LocalDate.of(2025, 1, 1);
    LocalTime startTime = LocalTime.of(18, 0);
    LocalTime endTime = LocalTime.of(20, 0);
    Show show = showTestDataBuilder.withTime(startTime, endTime).withDate(date).build();

    Seat seat = Seat.builder().seatNumber(1).grade(SeatGrade.A).build();
    seatRepository.save(seat);

    ShowSeat showSeat = ShowSeat.createAvailable(show, seat, 40000);
    showSeatRepository.save(showSeat);

    member = new Member("테스트유저", "abcd@gmail.com", "1234");
    memberRepository.save(member);
  }

  @Test
  @DisplayName("좌석 예약 성공")
  void testCreateBookingSuccessfully() {
    // When
    ShowSeat showSeat = showSeatRepository.findAll().get(0); // 또는 ID로 조회
    Long showSeatId = showSeat.getId();
    Long bookingId = bookingService.createBooking(requestId, member.getId(), List.of(showSeatId));

    // then
    assertThat(bookingId).isNotNull();
    ShowSeat updatedShowSeat = showSeatRepository.findById(showSeatId).orElseThrow();
    assertThat(updatedShowSeat.getStatus()).isEqualTo(Status.PAYMENT_PENDING);
  }

  @Test
  @DisplayName("존재하지 않는 좌석으로 예약을 할 수 없다.")
  void testCreateBookingWhenInvalidShowSeat() {
    // given
    Long memberId = member.getId();
    List<Long> showSeatIds = List.of(999L);

    // When & Then
    assertThatThrownBy(() -> bookingService.createBooking(requestId, memberId, showSeatIds))
        .isInstanceOf(BookingException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEAT_NOT_AVAILABLE);
  }

  @Test
  void givenDuplicateRequestId_whenCreateBooking_thenThrowException() {
    // given
    Long memberId = member.getId();
    List<Long> showSeatIds = List.of(showSeatRepository.findAll().get(0).getId());

    // when
    bookingService.createBooking(requestId, memberId, showSeatIds);

    // then

    assertThatThrownBy(() -> bookingService.createBooking(requestId, memberId, showSeatIds))
        .isInstanceOf(BookingException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_IDEMPOTENCY_KEY);
  }
}
