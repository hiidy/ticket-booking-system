package com.seatwise.show.service.strategy;

import com.seatwise.show.dto.request.ShowBookingRequest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ShowBookingV3StrategyIntegrationTest extends BookingStrategyIntegrationTestSupport {

  @Autowired private ShowBookingV3Strategy showBookingV3Strategy;

  @Test
  @DisplayName("V3(Local+Redis 락) - 동일 섹션 동시 예약 시 한 건만 성공")
  void v3_prevents_double_booking_with_local_and_multilock() throws Exception {
    ShowBookingRequest request = prepareBookingRequest();

    List<Object> results =
        runConcurrent(
            () -> showBookingV3Strategy.createBooking(randomKey(), request),
            () -> showBookingV3Strategy.createBooking(randomKey(), request));

    assertSingleSuccessAndSeatUnavailableFailure(results);
  }
}
