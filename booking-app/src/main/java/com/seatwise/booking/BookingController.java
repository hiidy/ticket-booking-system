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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(name = "예약 관리", description = "좌석 예약 관련 API")
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

  private final BookingMessageProducer producer;
  private final BookingService bookingService;
  private final AsyncBookingService asyncBookingService;
  private final BookingContext bookingContext;

  @Operation(summary = "예약 요청 생성", description = "비동기 방식으로 예약 요청을 생성하고 폴링 URL을 반환합니다")
  @PostMapping
  @ResponseStatus(HttpStatus.ACCEPTED)
  public BookingResponse createBookingRequest(
      @Parameter(description = "멱등성 키", required = true) @RequestHeader("Idempotency-Key")
          UUID idempotencyKey,
      @Valid @RequestBody BookingRequest request) {
    BookingCreateCommand command =
        BookingCreateCommand.of(request.memberId(), request.ticketIds(), request.sectionId());

    UUID requestId = asyncBookingService.createBookingRequest(idempotencyKey, command);
    String pollingUrl =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .pathSegment(requestId.toString(), "status")
            .build()
            .toUriString();

    return new BookingResponse(pollingUrl, requestId.toString());
  }

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

  @Operation(summary = "예약 상태 조회", description = "요청 ID로 예약 처리 상태를 조회합니다")
  @GetMapping("/{requestId}/status")
  public BookingStatusResponse getBookingStatus(
      @Parameter(description = "예약 요청 ID", required = true) @PathVariable UUID requestId) {
    return bookingService.getBookingStatus(requestId);
  }

  @Operation(summary = "예약 타임아웃 처리", description = "클라이언트 타임아웃 시 예약을 취소 처리합니다")
  @PostMapping("/{requestId}/timeout")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void timeoutBookingRequest(
      @Parameter(description = "예약 요청 ID", required = true) @PathVariable UUID requestId,
      @Valid @RequestBody BookingTimeoutRequest request) {
    producer.sendMessage(
        new BookingMessage(
            BookingMessageType.CLIENT_TIMEOUT_CANCEL,
            requestId.toString(),
            request.memberId(),
            null,
            request.sectionId()));
  }
}
