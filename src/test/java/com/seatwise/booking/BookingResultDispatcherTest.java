package com.seatwise.booking;

import static org.assertj.core.api.Assertions.assertThat;

import com.seatwise.booking.dto.BookingResult;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.async.DeferredResult;

class BookingResultDispatcherTest {

  private BookingResultDispatcher bookingResultDispatcher;

  @BeforeEach
  void setUp() {
    bookingResultDispatcher = new BookingResultDispatcher();
  }

  @Test
  void shouldReturnDeferredResult_whenWaitingForResult() {
    // given
    UUID requestId = UUID.randomUUID();

    // when
    DeferredResult<BookingResult> result = bookingResultDispatcher.waitForResult(requestId);

    // then
    assertThat(result).isNotNull();
    assertThat(result.hasResult()).isFalse();
  }

  @Test
  void shouldCompleteDeferredResult_whenResultIsProvided() {
    // given
    UUID requestId = UUID.randomUUID();
    DeferredResult<BookingResult> deferredResult = bookingResultDispatcher.waitForResult(requestId);
    BookingResult result = BookingResult.success(1L, requestId);

    // when
    bookingResultDispatcher.completeResult(requestId, result);

    // then
    assertThat(deferredResult.hasResult()).isTrue();
    assertThat(deferredResult.getResult()).isEqualTo(result);
  }
}
