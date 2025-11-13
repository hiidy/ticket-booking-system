package com.seatwise.booking;

import com.seatwise.booking.dto.BookingCreateCommand;
import com.seatwise.booking.dto.BookingMessage;
import com.seatwise.booking.dto.BookingMessageType;
import com.seatwise.booking.dto.request.BookingRequest;
import com.seatwise.booking.dto.request.BookingTimeoutRequest;
import com.seatwise.booking.dto.response.BookingResponse;
import com.seatwise.booking.dto.response.BookingStatusResponse;
import com.seatwise.booking.messaging.BookingMessageProducer;
import com.seatwise.booking.strategy.BookingContext;
import com.seatwise.booking.strategy.BookingVersion;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

  private final BookingMessageProducer producer;
  private final BookingService bookingService;
  private final AsyncBookingService asyncBookingService;
  private final SyncBookingService syncBookingService;
  private final BookingContext bookingContext;

  @PostMapping
  public ResponseEntity<BookingResponse> createBookingRequest(
      @RequestHeader("Idempotency-Key") UUID idempotencyKey,
      @Valid @RequestBody BookingRequest request) {
    BookingCreateCommand command =
        BookingCreateCommand.of(request.memberId(), request.ticketIds(), request.sectionId());

    UUID requestId = asyncBookingService.createBookingRequest(idempotencyKey, command);
    String pollingUrl =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .pathSegment(requestId.toString(), "status")
            .build()
            .toUriString();

    BookingResponse response = new BookingResponse(pollingUrl, requestId.toString());
    return ResponseEntity.accepted().body(response);
  }

  @PostMapping("/v1")
  public ResponseEntity<BookingStatusResponse> createBookingV1(
      @RequestHeader("Idempotency-Key") UUID key, @Valid @RequestBody BookingRequest request) {
    BookingStatusResponse response =
        bookingContext.get(BookingVersion.V1.getVersion()).createBooking(key, request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/sync/redis-lock")
  public ResponseEntity<BookingStatusResponse> createBookingWithRedisLock(
      @RequestHeader("Idempotency-Key") UUID key, @Valid @RequestBody BookingRequest request) {
    Long bookingId =
        syncBookingService.createWithRedisLock(key, request.memberId(), request.ticketIds());
    return ResponseEntity.ok(BookingStatusResponse.success(bookingId, key));
  }

  @GetMapping("/{requestId}/status")
  public ResponseEntity<BookingStatusResponse> getBookingStatus(@PathVariable UUID requestId) {
    BookingStatusResponse response = bookingService.getBookingStatus(requestId);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{requestId}/timeout")
  public ResponseEntity<Void> timeoutBookingRequest(
      @PathVariable UUID requestId, @Valid @RequestBody BookingTimeoutRequest request) {
    producer.sendMessage(
        new BookingMessage(
            BookingMessageType.CLIENT_TIMEOUT_CANCEL,
            requestId.toString(),
            request.memberId(),
            null,
            request.sectionId()));
    return ResponseEntity.accepted().build();
  }
}
