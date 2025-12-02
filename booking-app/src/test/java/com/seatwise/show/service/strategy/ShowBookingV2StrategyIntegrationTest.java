package com.seatwise.show.service.strategy;

import com.seatwise.show.dto.request.ShowBookingRequest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ShowBookingV2StrategyIntegrationTest extends BookingStrategyIntegrationTestSupport {

  @Autowired private ShowBookingV2Strategy showBookingV2Strategy;

  @Test
  @DisplayName("V2(Redisson 멀티락) - 동시 예약 시 한 건만 성공")
  void v2_prevents_double_booking_with_multilock() throws Exception {
    ShowBookingRequest request = prepareBookingRequest();

    List<Object> results =
        runConcurrent(
            () -> showBookingV2Strategy.createBooking(randomKey(), request),
            () -> showBookingV2Strategy.createBooking(randomKey(), request));

    assertSingleSuccessAndSeatUnavailableFailure(results);
  }
}
