package com.seatwise.venue.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.seatwise.common.exception.BadRequestException;
import com.seatwise.common.exception.ErrorCode;
import com.seatwise.seat.domain.Seat;
import com.seatwise.seat.domain.SeatType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VenueTest {

  @Test
  @DisplayName("Venue에 중복된 좌석을 저장하면 오류를 던진다.")
  void validateNewSeatNumbers_WithDuplicateInputs_ThrowsException() {
    // given
    Venue venue = new Venue("예술의 전당", 2000);
    Seat seat1 = new Seat(1, SeatType.A, venue);
    Seat seat2 = new Seat(2, SeatType.A, venue);
    venue.addSeat(seat1);
    venue.addSeat(seat2);
    List<Integer> seatNumbers = List.of(1, 2);

    // when & then
    assertThatThrownBy(() -> venue.validateNewSeatNumbers(seatNumbers))
        .isInstanceOf(BadRequestException.class)
        .hasMessage(ErrorCode.DUPLICATE_SEAT_NUMBER.getMessage());
  }
}
