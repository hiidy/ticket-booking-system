package com.seatwise.booking;

import static org.assertj.core.api.Assertions.assertThat;

import com.seatwise.booking.repository.RedisBookingRepository;
import com.seatwise.config.RedisTestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = {RedisTestConfig.class, RedisBookingRepository.class})
@Import(RedisTestConfig.class)
@ActiveProfiles("test")
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
    boolean result = repository.lockSeat(memberId, seatId);

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
    repository.lockSeat(memberId, seatId);

    // then
    boolean result = repository.lockSeat(2L, seatId);
    assertThat(result).isFalse();
  }
}
