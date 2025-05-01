package com.seatwise.show.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.seatwise.booking.domain.Booking;
import com.seatwise.common.exception.BadRequestException;
import com.seatwise.common.exception.ErrorCode;
import com.seatwise.member.domain.Member;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ShowSeatTest {

  @Test
  @DisplayName("예매 가능한 좌석을 예약하면 결제대기 상태가 된다..")
  void assignBooking_WithValidInputs_ChangedToPaymentPending() {
    // given
    ShowSeat showSeat = ShowSeat.createAvailable(null, null, 40000);
    Booking booking = new Booking(null, null, 0);

    // when
    showSeat.assignBooking(booking, LocalDateTime.now());

    // then
    assertThat(showSeat.getStatus()).isEqualTo(Status.PAYMENT_PENDING);
  }

  @Test
  @DisplayName("만료 시간이 끝나지 않은 좌석은 예약할 수 없다.")
  void assignBooking_BeforeExpirationTime_ThrowsException() {
    // given
    ShowSeat showSeat = ShowSeat.createAvailable(null, null, 40000);
    Booking booking = new Booking(null, null, 0);
    LocalDateTime requestTime = LocalDateTime.of(2025, 1, 1, 12, 0);
    LocalDateTime newRequestTime = requestTime.plusMinutes(1);
    showSeat.assignBooking(booking, requestTime);

    // when & then
    assertThatThrownBy(() -> showSeat.assignBooking(booking, newRequestTime))
        .isInstanceOf(BadRequestException.class)
        .hasMessage(ErrorCode.SEAT_NOT_AVAILABLE.getMessage());
  }

  @Test
  @DisplayName("좌석을 예약하지 않았지만 이용 가능한 상태가 아니라 예매할 경우 예외를 던진다")
  void assignBooking_WithNotBookedAndUnavailable_ThrowsException() {
    // given
    LocalDateTime requestTime = LocalDateTime.of(2025, 1, 1, 12, 0);
    ShowSeat showSeat = ShowSeat.createAvailable(null, null, 40000);
    showSeat.assignBooking(null, LocalDateTime.now());
    // when & then
    assertThatThrownBy(() -> showSeat.assignBooking(null, requestTime))
        .isInstanceOf(BadRequestException.class)
        .hasMessage(ErrorCode.SEAT_NOT_AVAILABLE.getMessage());
  }

  @Test
  @DisplayName("좌석의 가격이 0이하면 예외를 던진다")
  void createShowSeat_WithNegativePrice_ThrowsException() {
    assertThatThrownBy(() -> ShowSeat.createAvailable(null, null, -10000))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  @DisplayName("만료 시간 전에 예약을 하면 예외를 던진다.")
  void assignBookingBeforeExpiration() {
    // given
    ShowSeat showSeat = ShowSeat.createAvailable(null, null, 40000);
    Booking booking = new Booking(null, null, 0);
    LocalDateTime requestTime = LocalDateTime.of(2025, 1, 1, 12, 0);
    LocalDateTime newRequestTime = LocalDateTime.of(2025, 1, 1, 12, 5);
    showSeat.assignBooking(booking, requestTime);

    // when & then
    assertThatThrownBy(() -> showSeat.assignBooking(booking, newRequestTime))
        .isInstanceOf(BadRequestException.class)
        .hasMessage(ErrorCode.SEAT_NOT_AVAILABLE.getMessage());
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
    showSeat.assignBooking(booking1, LocalDateTime.of(2025, 1, 1, 12, 0));
    showSeat.assignBooking(booking2, LocalDateTime.of(2025, 1, 1, 12, 10));

    // then
    assertThat(showSeat.getBooking()).isEqualTo(booking2);
  }
}
