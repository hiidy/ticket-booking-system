package com.seatwise.show.service.strategy;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import com.seatwise.booking.dto.request.BookingRequest;
import com.seatwise.booking.exception.RecoverableBookingException;
import com.seatwise.core.BaseCode;
import com.seatwise.show.service.ShowBookingService;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

@ExtendWith(MockitoExtension.class)
class BookingV3StrategyTest {

  @Mock private ShowBookingService showBookingService;
  @Mock private RedissonClient redissonClient;
  @Mock private RLock multiLock;

  @InjectMocks private BookingV3Strategy bookingV3Strategy;

  private UUID testIdempotencyKey;
  private BookingRequest testRequest;

  @BeforeEach
  void setUp() {
    testIdempotencyKey = UUID.randomUUID();
    testRequest = new BookingRequest(1L, List.of(1L, 2L), 100L);
  }

  @Test
  @DisplayName("예매 성공 - 모든 락 획득 후 예매 완료")
  void creates_booking_successfully_when_all_locks_acquired() throws InterruptedException {
    // Given
    given(redissonClient.getMultiLock(eq("100"), any(List.class))).willReturn(multiLock);
    given(multiLock.tryLock(0L, 300L, TimeUnit.SECONDS)).willReturn(true);
    given(showBookingService.create(eq(testIdempotencyKey), eq(1L), any(List.class)))
        .willReturn("BOOKING-123");

    // When
    String result = bookingV3Strategy.createBooking(testIdempotencyKey, testRequest);

    // Then
    assertThat(result).isEqualTo("BOOKING-123");
    then(multiLock).should().unlock();
  }

  @Test
  @DisplayName("예매 실패 - Redis 락 획득 실패 시 SEAT_NOT_AVAILABLE 예외")
  void throws_seat_not_available_when_redis_lock_acquisition_fails() throws InterruptedException {
    // Given
    given(redissonClient.getMultiLock(eq("100"), any(List.class))).willReturn(multiLock);
    given(multiLock.tryLock(0L, 300L, TimeUnit.SECONDS)).willReturn(false);

    // When & Then
    assertThatThrownBy(() -> bookingV3Strategy.createBooking(testIdempotencyKey, testRequest))
        .isInstanceOf(RecoverableBookingException.class)
        .hasFieldOrPropertyWithValue("baseCode", BaseCode.SEAT_NOT_AVAILABLE);

    then(showBookingService).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("예매 실패 - Redis 락 획득 중 인터럽트 발생 시 LOCK_ACQUISITION_TIMEOUT 예외")
  void throws_lock_acquisition_timeout_when_interrupted_during_redis_lock()
      throws InterruptedException {
    // Given
    given(redissonClient.getMultiLock(eq("100"), any(List.class))).willReturn(multiLock);
    given(multiLock.tryLock(0L, 300L, TimeUnit.SECONDS))
        .willThrow(new InterruptedException("테스트 인터럽트"));

    Thread.currentThread().interrupt();

    // When & Then
    assertThatThrownBy(() -> bookingV3Strategy.createBooking(testIdempotencyKey, testRequest))
        .isInstanceOf(RecoverableBookingException.class)
        .hasFieldOrPropertyWithValue("baseCode", BaseCode.LOCK_ACQUISITION_TIMEOUT);

    // 인터럽트 상태 복원 확인
    assertThat(Thread.currentThread().isInterrupted()).isTrue();
  }
}
