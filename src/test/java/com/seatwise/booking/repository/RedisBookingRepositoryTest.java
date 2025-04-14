package com.seatwise.booking.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.seatwise.annotation.EmbeddedRedisTest;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
@EmbeddedRedisTest
@Disabled
class RedisBookingRepositoryTest {

  @Autowired private StringRedisTemplate redisTemplate;
  @Autowired private RedisBookingRepository repository;
  private static final String LOCK_PREFIX = "lock:showSeat:";

  @AfterEach
  void tearDown() {
    redisTemplate.getConnectionFactory().getConnection().flushAll();
  }

  @Test
  void lockSeatSuccess() {
    // given
    Long memberId = 1L;
    Long seatId = 1L;

    // when
    boolean result = repository.lockSeat(memberId, List.of(seatId));

    // then
    assertThat(result).isTrue();
    String lockValue = redisTemplate.opsForValue().get(LOCK_PREFIX + seatId);
    assertThat(memberId).hasToString(lockValue);
  }

  @Test
  void lockSeat_Failure_WithAlreadyLocked() {
    // given
    Long memberId = 1L;
    Long seatId = 1L;

    // when
    repository.lockSeat(memberId, List.of(seatId));

    // then
    boolean result = repository.lockSeat(2L, List.of(seatId));
    assertThat(result).isFalse();
  }

  @Test
  void concurrentLocking_Success_WithSameSeats() throws InterruptedException {
    // given
    int threadNum = 10;
    ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
    List<Long> seatIds = List.of(1L, 2L, 3L);
    CountDownLatch latch = new CountDownLatch(10);
    AtomicInteger count = new AtomicInteger(0);

    // when
    for (int i = 0; i < threadNum; i++) {
      long memberId = i + 1;
      executorService.submit(
          () -> {
            try {
              boolean locked = repository.lockSeat(memberId, seatIds);
              if (locked) {
                count.getAndIncrement();
              }
            } finally {
              latch.countDown();
            }
          });
    }
    latch.await();
    executorService.shutdown();

    // then
    assertThat(count.get()).isEqualTo(1);
  }
}
