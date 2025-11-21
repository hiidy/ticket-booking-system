package com.seatwise.show.service.strategy;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import com.seatwise.booking.dto.request.BookingRequest;
import com.seatwise.show.service.ShowBookingService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingV1StrategyTest {

  @Mock private ShowBookingService showBookingService;

  @InjectMocks private BookingV1Strategy bookingV1Strategy;

  private UUID testIdempotencyKey;
  private BookingRequest testRequest;

  @BeforeEach
  void setUp() {
    testIdempotencyKey = UUID.randomUUID();
    testRequest = new BookingRequest(1L, List.of(1L, 2L), 100L);
  }

  @Test
  @DisplayName("예매 성공 - 서비스에 위임하여 예매 완료")
  void delegates_to_show_booking_service_and_returns_result() {
    // Given
    given(showBookingService.createWithLock(eq(testIdempotencyKey), eq(1L), eq(List.of(1L, 2L))))
        .willReturn("BOOKING-123");

    // When
    String result = bookingV1Strategy.createBooking(testIdempotencyKey, testRequest);

    // Then
    assertThat(result).isEqualTo("BOOKING-123");
    then(showBookingService).should().createWithLock(testIdempotencyKey, 1L, List.of(1L, 2L));
  }

  @Test
  @DisplayName("예매 실패 - 서비스에서 예외 발생 시 그대로 전달")
  void propagates_exception_when_service_throws_exception() {
    // Given
    RuntimeException expectedException = new RuntimeException("서비스 오류");
    given(showBookingService.createWithLock(any(UUID.class), anyLong(), any(List.class)))
        .willThrow(expectedException);

    // When & Then
    assertThatThrownBy(() -> bookingV1Strategy.createBooking(testIdempotencyKey, testRequest))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("서비스 오류");
  }
}
