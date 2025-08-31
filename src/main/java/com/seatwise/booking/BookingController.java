package com.seatwise.booking;

import com.seatwise.booking.dto.BookingMessage;
import com.seatwise.booking.dto.BookingMessageType;
import com.seatwise.booking.dto.BookingTimeoutRequest;
import com.seatwise.booking.dto.request.BookingRequest;
import com.seatwise.booking.dto.response.BookingResponse;
import com.seatwise.booking.dto.response.BookingStatusResponse;
import com.seatwise.booking.messaging.BookingMessageProducer;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

  private final BookingMessageProducer producer;
  private final BookingService bookingService;

  @PostMapping
  public ResponseEntity<BookingResponse> createBookingRequest(
      @Valid @RequestBody BookingRequest request) {
    String requestId = UUID.randomUUID().toString();
    String pollingUrl =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .pathSegment(requestId, "status")
            .build()
            .toUriString();

    producer.sendMessage(
        new BookingMessage(
            BookingMessageType.BOOKING,
            requestId,
            request.memberId(),
            request.ticketIds(),
            request.sectionId()));

    BookingResponse response = new BookingResponse(pollingUrl, requestId);
    return ResponseEntity.accepted().body(response);
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
