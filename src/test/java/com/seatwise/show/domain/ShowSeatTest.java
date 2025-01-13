package com.seatwise.show.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.seatwise.booking.domain.Booking;
import com.seatwise.common.exception.BadRequestException;
import com.seatwise.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ShowSeatTest {

  @Test
  @DisplayName("예매 가능한 좌석을 예약하면 RESERVED로 상태가 변경된다.")
  void assignBooking_WithValidInputs_ChangedToReserved() {
    // given
    ShowSeat showSeat = ShowSeat.createAvailable(null, null, 40000);
    Booking booking = new Booking(null, null);

    // when
    showSeat.assignBooking(booking);

    // then
    assertThat(showSeat.getStatus()).isEqualTo(Status.RESERVED);
  }

  @Test
  @DisplayName("이미 예약된 좌석을 예매할 경우 예외를 던진다.")
  void assignBooking_WithAlreadyBookedSeat_ThrowsException() {
    // given
    ShowSeat showSeat = ShowSeat.createAvailable(null, null, 40000);
    Booking booking = new Booking(null, null);
    showSeat.assignBooking(booking);

    // when & then
    assertThatThrownBy(() -> showSeat.assignBooking(booking))
        .isInstanceOf(BadRequestException.class)
        .hasMessage(ErrorCode.SEAT_ALREADY_BOOKED.getMessage());
  }

  @Test
  @DisplayName("좌석을 예약하지 않았지만 이용 가능한 상태가 아니라 예매할 경우 예외를 던진다")
  void assignBooking_WithNotBookedAndUnavailable_ThrowsException() {
    // given
    ShowSeat showSeat = ShowSeat.createAvailable(null, null, 40000);
    showSeat.assignBooking(null);
    // when & then
    assertThatThrownBy(() -> showSeat.assignBooking(null))
        .isInstanceOf(BadRequestException.class)
        .hasMessage(ErrorCode.SEAT_NOT_AVAILABLE.getMessage());
  }

  @Test
  @DisplayName("좌석의 가격이 0이하면 예외를 던진다")
  void createShowSeat_WithNegativePrice_ThrowsException() {
    assertThatThrownBy(() -> ShowSeat.createAvailable(null, null, -10000))
        .isInstanceOf(BadRequestException.class);
  }
}
