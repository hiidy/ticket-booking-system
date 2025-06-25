package com.seatwise.ticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.seatwise.booking.domain.Booking;
import com.seatwise.core.BusinessException;
import com.seatwise.member.Member;
import com.seatwise.ticket.domain.Ticket;
import com.seatwise.ticket.domain.TicketStatus;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class TicketTest {

  @Test
  void shouldUpdateStatusToPending_whenAssigningBooking() {
    // given
    Ticket ticket = Ticket.createAvailable(null, null, 40000);
    Booking booking = new Booking(null, null, 0);

    // when
    ticket.assignBooking(booking.getId(), LocalDateTime.now(), Duration.ofMinutes(10));

    // then
    assertThat(ticket.getStatus()).isEqualTo(TicketStatus.PAYMENT_PENDING);
  }

  @Test
  void shouldUpdateExpirationTime_whenBookingAssigned() {
    // given
    Ticket ticket = Ticket.createAvailable(null, null, 40000);
    Booking booking = new Booking(null, null, 0);
    LocalDateTime requestTime = LocalDateTime.of(2025, 1, 1, 12, 0);
    Duration duration = Duration.ofMinutes(10);

    // when
    ticket.assignBooking(booking.getId(), requestTime, duration);

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
    ticket.assignBooking(booking.getId(), requestTime, duration);

    // then
    assertThat(ticket.getBookingId()).isEqualTo(booking.getId());
  }

  @Test
  void shouldReturnTrue_whenAssignAvailableTicket() {
    // given
    Ticket ticket = Ticket.createAvailable(null, null, 40000);
    LocalDateTime requestTime = LocalDateTime.of(2025, 1, 1, 12, 0);

    // expect
    assertThat(ticket.canAssignBooking(requestTime)).isTrue();
  }

  @Test
  void shouldReturnFalse_whenAssignBeforeExpiration() {
    // given
    Ticket ticket = Ticket.createAvailable(null, null, 40000);
    Booking booking = new Booking(null, null, 0);
    LocalDateTime requestTime = LocalDateTime.of(2025, 1, 1, 12, 0);
    LocalDateTime newRequestTime = requestTime.plusMinutes(1);
    ticket.assignBooking(booking.getId(), requestTime, Duration.ofMinutes(10));

    // expect
    assertThat(ticket.canAssignBooking(newRequestTime)).isFalse();
  }

  @Test
  void shouldThrowException_whenTicketPriceIsNegative() {
    // expect
    assertThatThrownBy(() -> Ticket.createAvailable(null, null, -10000))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void shouldOverrideBooking_whenAssignAfterExpiration() {
    // given
    Ticket ticket = Ticket.createAvailable(null, null, 40000);
    Member member1 = new Member("철수", "abcd@gamil.com", "1234");
    Member member2 = new Member("맹구", "qwer@gamil.com", "1234");
    Booking booking1 = new Booking(null, member1, 0);
    Booking booking2 = new Booking(null, member2, 0);

    // when
    ticket.assignBooking(
        booking1.getId(), LocalDateTime.of(2025, 1, 1, 12, 0), Duration.ofMinutes(10));
    ticket.assignBooking(
        booking2.getId(), LocalDateTime.of(2025, 1, 1, 12, 10), Duration.ofMinutes(10));

    // then
    assertThat(ticket.getBookingId()).isEqualTo(booking2.getId());
  }

  @Test
  void shouldUpdateStatus_whenCancelBooking() {
    // given
    Ticket ticket = Ticket.createAvailable(null, null, 40000);
    Member member = new Member("철수", "abcd@gamil.com", "1234");
    Booking booking = new Booking(null, member, 0);

    // when
    ticket.assignBooking(
        booking.getId(), LocalDateTime.of(2025, 1, 1, 12, 0), Duration.ofMinutes(10));
    ticket.cancelBooking();

    // then
    assertThat(ticket.getStatus()).isEqualTo(TicketStatus.CANCELLED);
  }

  @Test
  void shouldResetExpirationTimeAndBookingId_whenCancelBooking() {
    // given
    Ticket ticket = Ticket.createAvailable(null, null, 40000);
    Member member = new Member("철수", "abcd@gamil.com", "1234");
    Booking booking = new Booking(null, member, 0);

    // when
    ticket.assignBooking(
        booking.getId(), LocalDateTime.of(2025, 1, 1, 12, 0), Duration.ofMinutes(10));
    ticket.cancelBooking();

    // then
    assertThat(ticket.getBookingId()).isNull();
    assertThat(ticket.getExpirationTime()).isNull();
  }

  @Test
  void shouldInitializeTicketWithAvailableStatus() {
    // when
    Ticket ticket = Ticket.createAvailable(null, null, 40000);

    // then
    assertThat(ticket.getStatus()).isEqualTo(TicketStatus.AVAILABLE);
  }
}
