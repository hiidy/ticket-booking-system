package com.seatwise.showtime.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.seatwise.booking.domain.Booking;
import com.seatwise.core.BusinessException;
import com.seatwise.member.Member;
import com.seatwise.ticket.domain.Status;
import com.seatwise.ticket.domain.Ticket;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ShowTimeSeatTest {

  @Test
  void shouldUpdateStatusToPending_WhenAssigningBooking() {
    // given
    Ticket ticket = Ticket.createAvailable(null, null, 40000);
    Booking booking = new Booking(null, null, 0);

    // when
    ticket.assignBooking(booking, LocalDateTime.now(), Duration.ofMinutes(10));

    // then
    assertThat(ticket.getStatus()).isEqualTo(Status.PAYMENT_PENDING);
  }

  @Test
  void shouldUpdateExpirationTime_whenBookingAssigned() {
    // given
    Ticket ticket = Ticket.createAvailable(null, null, 40000);
    Booking booking = new Booking(null, null, 0);
    LocalDateTime requestTime = LocalDateTime.of(2025, 1, 1, 12, 0);
    Duration duration = Duration.ofMinutes(10);

    // when
    ticket.assignBooking(booking, requestTime, duration);

    // then
    assertThat(ticket.getExpirationTime()).isEqualTo(requestTime.plus(duration));
  }

  @Test
  void shouldUpdateBooking_whenBookingAssigned() {
    // given
    Ticket ticket = Ticket.createAvailable(null, null, 40000);
    Booking booking = new Booking(null, null, 0);
    LocalDateTime requestTime = LocalDateTime.of(2025, 1, 1, 12, 0);
    Duration duration = Duration.ofMinutes(10);

    // when
    ticket.assignBooking(booking, requestTime, duration);

    // then
    assertThat(ticket.getBooking()).isEqualTo(booking);
  }

  @Test
  void shouldReturnTrue_whenAssignAvailableSeat() {
    // given
    Ticket ticket = Ticket.createAvailable(null, null, 40000);
    LocalDateTime requestTime = LocalDateTime.of(2025, 1, 1, 12, 0);

    // when & then
    assertThat(ticket.canAssignBooking(requestTime)).isTrue();
  }

  @Test
  void shouldReturnFalse_whenAssignBeforeExpiration() {
    // given
    Ticket ticket = Ticket.createAvailable(null, null, 40000);
    Booking booking = new Booking(null, null, 0);
    LocalDateTime requestTime = LocalDateTime.of(2025, 1, 1, 12, 0);
    LocalDateTime newRequestTime = requestTime.plusMinutes(1);
    ticket.assignBooking(booking, requestTime, Duration.ofMinutes(10));

    // when & then
    assertThat(ticket.canAssignBooking(newRequestTime)).isFalse();
  }

  @Test
  @DisplayName("좌석의 가격이 0이하면 예외를 던진다")
  void createShowSeat_WithNegativePrice_ThrowsException() {
    assertThatThrownBy(() -> Ticket.createAvailable(null, null, -10000))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("만료 시간 후에 예약을 하면 예약에 성공하고 결제 대기자 변경.")
  void assignBookingAfterExpiration() {
    // given
    Ticket ticket = Ticket.createAvailable(null, null, 40000);
    Member member1 = new Member("철수", "abcd@gamil.com", "1234");
    Member member2 = new Member("맹구", "qwer@gamil.com", "1234");
    Booking booking1 = new Booking(null, member1, 0);
    Booking booking2 = new Booking(null, member2, 0);

    // when & then
    ticket.assignBooking(booking1, LocalDateTime.of(2025, 1, 1, 12, 0), Duration.ofMinutes(10));
    ticket.assignBooking(booking2, LocalDateTime.of(2025, 1, 1, 12, 10), Duration.ofMinutes(10));

    // then
    assertThat(ticket.getBooking()).isEqualTo(booking2);
  }

  @Test
  void shouldReturnAvailableShowSeat_whenInitializeShowSeat() {
    // given
    Ticket ticket = Ticket.createAvailable(null, null, 40000);

    // then
    assertThat(ticket.getStatus()).isEqualTo(Status.AVAILABLE);
  }
}
