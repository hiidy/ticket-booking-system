package com.seatwise.show.service.strategy;

import com.seatwise.show.dto.request.ShowBookingRequest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ShowBookingV1StrategyIntegrationTest extends BookingStrategyIntegrationTestSupport {

  @Autowired private ShowBookingV1Strategy showBookingV1Strategy;

  @Test
  @DisplayName("V1(DB 비관적 락) - 동시 예약 시 한 건만 성공하고 나머지는 좌석 불가")
  void v1_prevents_double_booking_with_pessimistic_lock() throws Exception {
    ShowBookingRequest request = prepareBookingRequest();

    List<Object> results =
        runConcurrent(
            () -> showBookingV1Strategy.createBooking(randomKey(), request),
            () -> showBookingV1Strategy.createBooking(randomKey(), request));

    assertSingleSuccessAndSeatUnavailableFailure(results);
  }
}
