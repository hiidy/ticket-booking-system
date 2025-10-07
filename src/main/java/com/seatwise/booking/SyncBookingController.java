package com.seatwise.booking;

import com.seatwise.booking.dto.request.BookingRequest;
import com.seatwise.booking.dto.response.BookingStatusResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class SyncBookingController {

  private final SyncBookingService syncBookingService;

  @PostMapping("/sync")
  public ResponseEntity<BookingStatusResponse> createBookingSync(
      @RequestHeader("Idempotency-Key") UUID key, @Valid @RequestBody BookingRequest request) {
    Long bookingId =
        syncBookingService.createBookingSync(key, request.memberId(), request.ticketIds());
    return ResponseEntity.ok(BookingStatusResponse.success(bookingId, key));
  }
}
