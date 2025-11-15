package com.seatwise.show;

import com.seatwise.booking.dto.request.BookingRequest;
import com.seatwise.show.service.strategy.BookingContext;
import com.seatwise.show.service.strategy.BookingVersion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "공연 예매", description = "공연 예매 관련 API")
@RestController
@RequestMapping("/api/show/booking")
@RequiredArgsConstructor
public class ShowBookingController {

  private final BookingContext bookingContext;

  @Operation(summary = "예약 생성 v1", description = "DB 비관적락으로 예약 처리")
  @PostMapping("/v1")
  public String createBookingV1(
      @Parameter(description = "멱등성 키", required = true) @RequestHeader("Idempotency-Key") UUID key,
      @Valid @RequestBody BookingRequest request) {
    return bookingContext.get(BookingVersion.V1.getVersion()).createBooking(key, request);
  }

  @Operation(summary = "예약 생성 v2", description = "Redis Lock으로 예약 처리")
  @PostMapping("/v2")
  public String createBookingV2(
      @Parameter(description = "멱등성 키", required = true) @RequestHeader("Idempotency-Key") UUID key,
      @Valid @RequestBody BookingRequest request) {
    return bookingContext.get(BookingVersion.V2.getVersion()).createBooking(key, request);
  }
}
