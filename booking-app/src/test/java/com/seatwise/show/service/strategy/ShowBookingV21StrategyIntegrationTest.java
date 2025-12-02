package com.seatwise.show.service.strategy;

import com.seatwise.show.dto.request.ShowBookingRequest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ShowBookingV21StrategyIntegrationTest extends BookingStrategyIntegrationTestSupport {

  @Autowired private ShowBookingV21Strategy showBookingV21Strategy;

  @Test
  @DisplayName("V21(Faster MultiLock) - 동시 예약 시 한 건만 성공")
  void v21_prevents_double_booking_with_faster_multilock() throws Exception {
    ShowBookingRequest request = prepareBookingRequest();

    List<Object> results =
        runConcurrent(
            () -> showBookingV21Strategy.createBooking(randomKey(), request),
            () -> showBookingV21Strategy.createBooking(randomKey(), request));

    assertSingleSuccessAndSeatUnavailableFailure(results);
  }
}
