package com.seatwise.show.ticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.seatwise.booking.entity.Booking;
import com.seatwise.booking.entity.BookingStatus;
import com.seatwise.core.exception.BusinessException;
import com.seatwise.show.entity.Ticket;
import com.seatwise.show.entity.TicketStatus;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TicketTest {

  @Test
  @DisplayName("티켓에 예매를 할당하면 상태가 결제 대기 상태로 변경된다")
  void shouldUpdateStatusToPending_whenAssigningBooking() {
    // given
    Ticket ticket = Ticket.createAvailable(null, null, 1L, 40000);
    Long bookingId = 123L;

    // when
    ticket.assignBooking(bookingId, LocalDateTime.now(), Duration.ofMinutes(10));

    // then
    assertThat(ticket.getStatus()).isEqualTo(TicketStatus.PAYMENT_PENDING);
  }

  @Test
  @DisplayName("티켓에 예매를 할당하면 만료 시간이 설정된다")
  void shouldUpdateExpirationTime_whenBookingAssigned() {
    // given
    Ticket ticket = Ticket.createAvailable(null, null, 1L, 40000);
    Long bookingId = 123L;
    LocalDateTime requestTime = LocalDateTime.of(2025, 1, 1, 12, 0);
    Duration duration = Duration.ofMinutes(10);

    // when
    ticket.assignBooking(bookingId, requestTime, duration);

    // then
    assertThat(ticket.getExpirationTime()).isEqualTo(requestTime.plus(duration));
  }

  @Test
  @DisplayName("티켓에 예매를 할당하면 bookingId가 설정된다")
  void shouldUpdateBooking_whenBookingAssigned() {
    // given
    Ticket ticket = Ticket.createAvailable(null, null, 1L, 40000);
    Long bookingId = 123L;
    LocalDateTime requestTime = LocalDateTime.of(2025, 1, 1, 12, 0);
    Duration duration = Duration.ofMinutes(10);

    // when
    ticket.assignBooking(bookingId, requestTime, duration);

    // then
    assertThat(ticket.getBookingId()).isEqualTo(bookingId);
  }

  @Test
  @DisplayName("예매 가능한 티켓은 예매 할당이 가능하다")
  void shouldReturnTrue_whenAssignAvailableTicket() {
    // given
    Ticket ticket = Ticket.createAvailable(null, null, 1L, 40000);
    LocalDateTime requestTime = LocalDateTime.of(2025, 1, 1, 12, 0);

    // expect
    assertThat(ticket.canAssignBooking(requestTime)).isTrue();
  }

  @Test
  @DisplayName("만료 시간이 지나지 않은 티켓은 예매 할당이 불가능하다")
  void shouldReturnFalse_whenAssignBeforeExpiration() {
    // given
    Ticket ticket = Ticket.createAvailable(null, null, 1L, 40000);
    Long bookingId = 123L;
    LocalDateTime requestTime = LocalDateTime.of(2025, 1, 1, 12, 0);
    LocalDateTime newRequestTime = requestTime.plusMinutes(1);
    ticket.assignBooking(bookingId, requestTime, Duration.ofMinutes(10));

    // expect
    assertThat(ticket.canAssignBooking(newRequestTime)).isFalse();
  }

  @Test
  @DisplayName("티켓 가격이 음수이면 예외가 발생한다")
  void shouldThrowException_whenTicketPriceIsNegative() {
    // expect
    assertThatThrownBy(() -> Ticket.createAvailable(null, null, 1L, -10000))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("만료 시간이 지난 후 예매를 할당하면 기존 예매가 덮어씌워진다")
  void shouldOverrideBooking_whenAssignAfterExpiration() {
    // given
    Ticket ticket = Ticket.createAvailable(null, null, 1L, 40000);
    Long bookingId1 = 123L;
    Long bookingId2 = 456L;

    // when
    ticket.assignBooking(bookingId1, LocalDateTime.of(2025, 1, 1, 12, 0), Duration.ofMinutes(10));
    ticket.assignBooking(bookingId2, LocalDateTime.of(2025, 1, 1, 12, 10), Duration.ofMinutes(10));

    // then
    assertThat(ticket.getBookingId()).isEqualTo(bookingId2);
  }

  @Test
  @DisplayName("티켓 예매를 취소하면 상태가 취소 상태로 변경된다")
  void shouldUpdateStatus_whenCancelBooking() {
    // given
    Ticket ticket = Ticket.createAvailable(null, null, 1L, 40000);
    Long bookingId = 123L;

    // when
    ticket.assignBooking(bookingId, LocalDateTime.of(2025, 1, 1, 12, 0), Duration.ofMinutes(10));
    ticket.cancelBooking();

    // then
    assertThat(ticket.getStatus()).isEqualTo(TicketStatus.CANCELLED);
  }

  @Test
  @DisplayName("티켓 예매를 취소하면 만료 시간과 bookingId가 초기화된다")
  void shouldResetExpirationTimeAndBookingId_whenCancelBooking() {
    // given
    Ticket ticket = Ticket.createAvailable(null, null, 1L, 40000);
    Long bookingId = 123L;

    // when
    ticket.assignBooking(bookingId, LocalDateTime.of(2025, 1, 1, 12, 0), Duration.ofMinutes(10));
    ticket.cancelBooking();

    // then
    assertThat(ticket.getBookingId()).isNull();
    assertThat(ticket.getExpirationTime()).isNull();
  }

  @Test
  @DisplayName("새로 생성된 티켓은 예매 가능 상태로 초기화된다")
  void shouldInitializeTicketWithAvailableStatus() {
    // when
    Ticket ticket = Ticket.createAvailable(null, null, 1L, 40000);

    // then
    assertThat(ticket.getStatus()).isEqualTo(TicketStatus.AVAILABLE);
  }

  @Test
  @DisplayName("Booking 생성 시나리오 -> 대기 상태로 생성 후 성공 상태로 변경")
  void testBookingCreationFlow() {
    // given & when
    UUID requestId = UUID.randomUUID();
    Long memberId = 1L;
    int totalAmount = 50000;

    Booking booking = Booking.createNew(requestId, memberId, totalAmount);

    // then
    assertThat(booking.getRequestId()).isEqualTo(requestId);
    assertThat(booking.getMemberId()).isEqualTo(memberId);
    assertThat(booking.getTotalAmount()).isEqualTo(totalAmount);
    assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);

    // when
    booking.markAsSuccess();

    // then
    assertThat(booking.getStatus()).isEqualTo(BookingStatus.SUCCESS);
  }

  @Test
  @DisplayName("Booking 실패 시나리오: 대기 상태로 생성 후 실패 상태로 변경")
  void testBookingFailedFlow() {
    // given
    UUID requestId = UUID.randomUUID();
    Long memberId = 1L;

    // when
    Booking booking = Booking.createNew(requestId, memberId, 100000);
    booking.markAsFailed();

    // then
    assertThat(booking.getStatus()).isEqualTo(BookingStatus.FAILED);
    assertThat(booking.getTotalAmount()).isEqualTo(0);
  }
}
