package com.seatwise.booking;

import static org.assertj.core.api.Assertions.assertThat;

import com.seatwise.booking.dto.response.BookingResponse;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.async.DeferredResult;

class BookingResponseManagerTest {

  private BookingResponseManager bookingResponseManager;

  @BeforeEach
  void setUp() {
    bookingResponseManager = new BookingResponseManager();
  }

  @Test
  void shouldReturnDeferredResult_whenWaitingForResult() {
    // given
    UUID requestId = UUID.randomUUID();

    // when
    DeferredResult<BookingResponse> result =
        bookingResponseManager.createPendingResponse(requestId);

    // then
    assertThat(result).isNotNull();
    assertThat(result.hasResult()).isFalse();
  }

  @Test
  void shouldCompleteDeferredResult_whenResultIsProvided() {
    // given
    UUID requestId = UUID.randomUUID();
    DeferredResult<BookingResponse> deferredResult =
        bookingResponseManager.createPendingResponse(requestId);
    BookingResponse result = BookingResponse.success(1L, requestId);

    // when
    bookingResponseManager.completeWithSuccess(requestId, result);

    // then
    assertThat(deferredResult.hasResult()).isTrue();
    assertThat(deferredResult.getResult()).isEqualTo(result);
  }
}
