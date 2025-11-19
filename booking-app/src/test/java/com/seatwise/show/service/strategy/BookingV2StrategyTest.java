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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class BookingV2StrategyTest {

  @Mock private ShowBookingService showBookingService;
  @Mock private RedissonClient redissonClient;
  @Mock private RLock seatLock1;
  @Mock private RLock seatLock2;
  @Mock private RLock multiLock;

  @InjectMocks
  private BookingV2Strategy bookingV2Strategy;

  private UUID testIdempotencyKey;
  private BookingRequest testRequest;

  @BeforeEach
  void setUp() {
    testIdempotencyKey = UUID.randomUUID();
    testRequest = new BookingRequest(1L, List.of(2L, 1L), 100L); // 정렬된 순서와 다르게
  }

  @Test
  @DisplayName("예매 성공 - 정렬된 티켓 ID로 락 획득 후 예매 완료")
  void creates_booking_successfully_when_ticket_locks_acquired() throws InterruptedException {
    // Given
    // V2는 티켓 ID를 정렬하여 락을 생성함 (1L, 2L 순서)
    given(redissonClient.getLock("lock:seat:1")).willReturn(seatLock1);
    given(redissonClient.getLock("lock:seat:2")).willReturn(seatLock2);
    given(redissonClient.getMultiLock(seatLock1, seatLock2)).willReturn(multiLock);

    given(multiLock.tryLock(0L, 300L, TimeUnit.SECONDS))
        .willReturn(true);
    given(showBookingService.create(eq(testIdempotencyKey), eq(1L), eq(List.of(2L, 1L))))
        .willReturn("BOOKING-123");

    // When
    String result = bookingV2Strategy.createBooking(testIdempotencyKey, testRequest);

    // Then
    assertThat(result).isEqualTo("BOOKING-123");

    // 티켓 ID가 정렬되어 락이 생성되었는지 확인
    then(redissonClient).should().getLock("lock:seat:1");
    then(redissonClient).should().getLock("lock:seat:2");
    then(redissonClient).should().getMultiLock(seatLock1, seatLock2);

    then(multiLock).should().unlock();
  }

  @Test
  @DisplayName("예매 실패 - 티켓 락 획득 실패 시 SEAT_NOT_AVAILABLE 예외")
  void throws_seat_not_available_when_ticket_lock_acquisition_fails() throws InterruptedException {
    // Given
    given(redissonClient.getLock("lock:seat:1")).willReturn(seatLock1);
    given(redissonClient.getLock("lock:seat:2")).willReturn(seatLock2);
    given(redissonClient.getMultiLock(seatLock1, seatLock2)).willReturn(multiLock);

    given(multiLock.tryLock(0L, 300L, TimeUnit.SECONDS))
        .willReturn(false);

    // When & Then
    assertThatThrownBy(() -> bookingV2Strategy.createBooking(testIdempotencyKey, testRequest))
        .isInstanceOf(RecoverableBookingException.class)
        .hasFieldOrPropertyWithValue("baseCode", BaseCode.SEAT_NOT_AVAILABLE);

    then(showBookingService).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("예매 실패 - 티켓 락 획득 중 인터럽트 발생 시 LOCK_ACQUISITION_TIMEOUT 예외")
  void throws_lock_acquisition_timeout_when_interrupted_during_ticket_lock() throws InterruptedException {
    // Given
    given(redissonClient.getLock("lock:seat:1")).willReturn(seatLock1);
    given(redissonClient.getLock("lock:seat:2")).willReturn(seatLock2);
    given(redissonClient.getMultiLock(seatLock1, seatLock2)).willReturn(multiLock);

    given(multiLock.tryLock(0L, 300L, TimeUnit.SECONDS))
        .willThrow(new InterruptedException("테스트 인터럽트"));

    Thread.currentThread().interrupt();

    // When & Then
    assertThatThrownBy(() -> bookingV2Strategy.createBooking(testIdempotencyKey, testRequest))
        .isInstanceOf(RecoverableBookingException.class)
        .hasFieldOrPropertyWithValue("baseCode", BaseCode.LOCK_ACQUISITION_TIMEOUT);

    // 인터럽트 상태 복원 확인
    assertThat(Thread.currentThread().isInterrupted()).isTrue();
  }
}