package com.seatwise.booking.service;

import static org.assertj.core.api.Assertions.*;

import com.seatwise.booking.dto.BookingResult;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.async.DeferredResult;

class BookingResultWaitServiceTest {

  private BookingResultWaitService bookingResultWaitService;

  @BeforeEach
  void setUp() {
    bookingResultWaitService = new BookingResultWaitService();
  }

  @Test
  void givenValidRequest_whenWaitForResult_thenReturnNewDeferredResult() {
    // given
    UUID requestId = UUID.randomUUID();

    // when
    DeferredResult<BookingResult> result = bookingResultWaitService.waitForResult(requestId);

    // then
    assertThat(result).isNotNull();
    assertThat(result.hasResult()).isFalse();
  }

  @Test
  void givenValidRequest_whenCompleteResult_thenSetDeferredResult() {
    // given
    UUID requestId = UUID.randomUUID();
    DeferredResult<BookingResult> deferredResult =
        bookingResultWaitService.waitForResult(requestId);
    Long bookingId = 1L;
    BookingResult result = BookingResult.success(bookingId, requestId);

    // when
    bookingResultWaitService.completeResult(requestId, result);

    // then
    assertThat(deferredResult.hasResult()).isTrue();
    assertThat(deferredResult.getResult()).isEqualTo(result);
  }
}
