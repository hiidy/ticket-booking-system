package com.seatwise.show.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.seatwise.booking.entity.Booking;
import com.seatwise.common.exception.BusinessException;
import com.seatwise.member.entity.Member;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ShowSeatTest {

  @Test
  void shouldUpdateStatusToPending_WhenAssigningBooking() {
    // given
    ShowSeat showSeat = ShowSeat.createAvailable(null, null, 40000);
    Booking booking = new Booking(null, null, 0);

    // when
    showSeat.assignBooking(booking, LocalDateTime.now(), Duration.ofMinutes(10));

    // then
    assertThat(showSeat.getStatus()).isEqualTo(Status.PAYMENT_PENDING);
  }

  @Test
  void shouldUpdateExpirationTime_whenBookingAssigned() {
    // given
    ShowSeat showSeat = ShowSeat.createAvailable(null, null, 40000);
    Booking booking = new Booking(null, null, 0);
    LocalDateTime requestTime = LocalDateTime.of(2025, 1, 1, 12, 0);
    Duration duration = Duration.ofMinutes(10);

    // when
    showSeat.assignBooking(booking, requestTime, duration);

    // then
    assertThat(showSeat.getExpirationTime()).isEqualTo(requestTime.plus(duration));
  }

  @Test
  void shouldUpdateBooking_whenBookingAssigned() {
    // given
    ShowSeat showSeat = ShowSeat.createAvailable(null, null, 40000);
    Booking booking = new Booking(null, null, 0);
    LocalDateTime requestTime = LocalDateTime.of(2025, 1, 1, 12, 0);
    Duration duration = Duration.ofMinutes(10);

    // when
    showSeat.assignBooking(booking, requestTime, duration);

    // then
    assertThat(showSeat.getBooking()).isEqualTo(booking);
  }

  @Test
  void shouldReturnTrue_whenAssignAvailableSeat() {
    // given
    ShowSeat showSeat = ShowSeat.createAvailable(null, null, 40000);
    LocalDateTime requestTime = LocalDateTime.of(2025, 1, 1, 12, 0);

    // when & then
    assertThat(showSeat.canAssignBooking(requestTime)).isTrue();
  }

  @Test
  void shouldReturnFalse_whenAssignBeforeExpiration() {
    // given
    ShowSeat showSeat = ShowSeat.createAvailable(null, null, 40000);
    Booking booking = new Booking(null, null, 0);
    LocalDateTime requestTime = LocalDateTime.of(2025, 1, 1, 12, 0);
    LocalDateTime newRequestTime = requestTime.plusMinutes(1);
    showSeat.assignBooking(booking, requestTime, Duration.ofMinutes(10));

    // when & then
    assertThat(showSeat.canAssignBooking(newRequestTime)).isFalse();
  }

  @Test
  @DisplayName("좌석의 가격이 0이하면 예외를 던진다")
  void createShowSeat_WithNegativePrice_ThrowsException() {
    assertThatThrownBy(() -> ShowSeat.createAvailable(null, null, -10000))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("만료 시간 후에 예약을 하면 예약에 성공하고 결제 대기자 변경.")
  void assignBookingAfterExpiration() {
    // given
    ShowSeat showSeat = ShowSeat.createAvailable(null, null, 40000);
    Member member1 = new Member("철수", "abcd@gamil.com", "1234");
    Member member2 = new Member("맹구", "qwer@gamil.com", "1234");
    Booking booking1 = new Booking(null, member1, 0);
    Booking booking2 = new Booking(null, member2, 0);

    // when & then
    showSeat.assignBooking(booking1, LocalDateTime.of(2025, 1, 1, 12, 0), Duration.ofMinutes(10));
    showSeat.assignBooking(booking2, LocalDateTime.of(2025, 1, 1, 12, 10), Duration.ofMinutes(10));

    // then
    assertThat(showSeat.getBooking()).isEqualTo(booking2);
  }

  @Test
  void shouldReturnAvailableShowSeat_whenInitializeShowSeat() {
    // given
    ShowSeat showSeat = ShowSeat.createAvailable(null, null, 40000);

    // then
    assertThat(showSeat.getStatus()).isEqualTo(Status.AVAILABLE);
  }
}
