package com.seatwise.cache.local;

import static org.assertj.core.api.Assertions.*;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.seatwise.cache.config.CacheProperties;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("LocalTicketCacheService 테스트")
class LocalTicketCacheServiceTest {

  private LocalTicketCacheService cacheService;

  @BeforeEach
  void setUp() {
    CacheProperties cacheProperties = createDefaultCacheProperties();
    cacheService = new LocalTicketCacheService(cacheProperties);
  }

  private CacheProperties createDefaultCacheProperties() {
    return new CacheProperties(
        new CacheProperties.LocalCacheProperties(
            true, // enabled
            10_000, // maxSize
            Duration.ofMinutes(10), // ttl
            true // recordStats
            ),
        new CacheProperties.RedisCacheProperties(true, Duration.ofMinutes(10)),
        new CacheProperties.MultiLevelCacheProperties(true, "cache:invalidation", true, true));
  }

  @Nested
  @DisplayName("티켓 홀드 테스트")
  class HoldTicketsTest {

    @Test
    @DisplayName("단일 티켓을 성공적으로 홀드한다")
    void holdSingleTicket() {
      // given
      Long ticketId = 1L;
      Long memberId = 100L;

      // when
      cacheService.holdTicket(ticketId, memberId);

      // then
      Optional<Long> result = cacheService.getHoldMember(ticketId);
      assertThat(result).isPresent();
      assertThat(result.get()).isEqualTo(memberId);
    }

    @Test
    @DisplayName("여러 티켓을 한번에 홀드한다")
    void holdMultipleTickets() {
      // given
      List<Long> ticketIds = List.of(1L, 2L, 3L, 4L, 5L);
      Long memberId = 100L;

      // when
      cacheService.holdTickets(ticketIds, memberId);

      // then
      Map<Long, Long> holdMembers = cacheService.getHoldMembers(ticketIds);
      assertThat(holdMembers).hasSize(5);
      assertThat(holdMembers.values()).allMatch(id -> id.equals(memberId));
    }

    @Test
    @DisplayName("null 티켓 ID로 홀드 시도 시 예외가 발생하지 않는다")
    void holdNullTicketId() {
      // given
      Long ticketId = null;
      Long memberId = 100L;

      // when & then
      assertThatCode(() -> cacheService.holdTicket(ticketId, memberId)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("빈 리스트로 홀드 시도 시 예외가 발생하지 않는다")
    void holdEmptyList() {
      // given
      List<Long> ticketIds = Collections.emptyList();
      Long memberId = 100L;

      // when & then
      assertThatCode(() -> cacheService.holdTickets(ticketIds, memberId))
          .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이미 홀드된 티켓을 다른 멤버가 덮어쓸 수 있다")
    void overwriteHoldTicket() {
      // given
      Long ticketId = 1L;
      Long firstMemberId = 100L;
      Long secondMemberId = 200L;

      cacheService.holdTicket(ticketId, firstMemberId);

      // when
      cacheService.holdTicket(ticketId, secondMemberId);

      // then
      Optional<Long> result = cacheService.getHoldMember(ticketId);
      assertThat(result).isPresent();
      assertThat(result.get()).isEqualTo(secondMemberId);
    }
  }

  @Nested
  @DisplayName("티켓 가용성 체크 테스트")
  class AvailabilityCheckTest {

    @Test
    @DisplayName("다른 멤버가 홀드한 티켓이 있으면 사용 불가능하다")
    void hasUnavailableTicketsWhenHeldByOther() {
      // given - 실제 시나리오: 멤버 A가 티켓을 먼저 홀드
      List<Long> ticketIds = List.of(1L, 2L, 3L);
      Long memberA = 100L;
      Long memberB = 200L;

      cacheService.holdTickets(ticketIds, memberA);

      // when - 멤버 B가 같은 티켓 예약 시도
      boolean unavailable = cacheService.hasUnavailableTickets(ticketIds, memberB);

      // then
      assertThat(unavailable).isTrue();
    }

    @Test
    @DisplayName("같은 멤버가 홀드한 티켓이면 사용 가능하다")
    void availableWhenHeldBySameMember() {
      // given
      List<Long> ticketIds = List.of(1L, 2L, 3L);
      Long memberId = 100L;

      cacheService.holdTickets(ticketIds, memberId);

      // when
      boolean unavailable = cacheService.hasUnavailableTickets(ticketIds, memberId);

      // then
      assertThat(unavailable).isFalse();
    }

    @Test
    @DisplayName("일부 티켓만 다른 멤버가 홀드한 경우 사용 불가능하다")
    void unavailableWhenPartiallyHeld() {
      // given - 실제 시나리오: 3개 중 1개만 다른 멤버가 홀드
      Long ticketId1 = 1L;
      Long ticketId2 = 2L;
      Long ticketId3 = 3L;
      Long memberA = 100L;
      Long memberB = 200L;

      cacheService.holdTicket(ticketId1, memberA);
      // ticketId2, ticketId3는 홀드 안됨

      // when - 멤버 B가 세 티켓 모두 예약 시도
      List<Long> requestedTickets = List.of(ticketId1, ticketId2, ticketId3);
      boolean unavailable = cacheService.hasUnavailableTickets(requestedTickets, memberB);

      // then
      assertThat(unavailable).isTrue();
    }

    @Test
    @DisplayName("홀드되지 않은 티켓들은 모두 사용 가능하다")
    void availableWhenNotHeld() {
      // given
      List<Long> ticketIds = List.of(1L, 2L, 3L);
      Long memberId = 100L;

      // when - 아무도 홀드하지 않은 상태
      boolean unavailable = cacheService.hasUnavailableTickets(ticketIds, memberId);

      // then
      assertThat(unavailable).isFalse();
    }
  }

  @Nested
  @DisplayName("티켓 조회 테스트")
  class GetHoldMemberTest {

    @Test
    @DisplayName("홀드된 티켓의 멤버 ID를 조회한다")
    void getHoldMember() {
      // given
      Long ticketId = 1L;
      Long memberId = 100L;
      cacheService.holdTicket(ticketId, memberId);

      // when
      Optional<Long> result = cacheService.getHoldMember(ticketId);

      // then
      assertThat(result).isPresent();
      assertThat(result.get()).isEqualTo(memberId);
    }

    @Test
    @DisplayName("홀드되지 않은 티켓 조회 시 empty를 반환한다")
    void getHoldMemberNotFound() {
      // given
      Long ticketId = 999L;

      // when
      Optional<Long> result = cacheService.getHoldMember(ticketId);

      // then
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("여러 티켓의 홀드 정보를 한번에 조회한다")
    void getHoldMembers() {
      // given - 여러 멤버가 각자 티켓 홀드
      cacheService.holdTicket(1L, 100L);
      cacheService.holdTicket(2L, 200L);
      cacheService.holdTicket(3L, 300L);

      // when
      Map<Long, Long> result = cacheService.getHoldMembers(List.of(1L, 2L, 3L, 4L));

      // then
      assertThat(result).hasSize(3);
      assertThat(result.get(1L)).isEqualTo(100L);
      assertThat(result.get(2L)).isEqualTo(200L);
      assertThat(result.get(3L)).isEqualTo(300L);
      assertThat(result.get(4L)).isNull(); // 홀드 안된 티켓
    }
  }

  @Nested
  @DisplayName("티켓 해제 테스트")
  class ReleaseTicketsTest {

    @Test
    @DisplayName("단일 티켓을 해제한다")
    void releaseSingleTicket() {
      // given
      Long ticketId = 1L;
      Long memberId = 100L;
      cacheService.holdTicket(ticketId, memberId);

      // when
      cacheService.releaseTicket(ticketId);

      // then
      Optional<Long> result = cacheService.getHoldMember(ticketId);
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("여러 티켓을 한번에 해제한다")
    void releaseMultipleTickets() {
      // given
      List<Long> ticketIds = List.of(1L, 2L, 3L);
      Long memberId = 100L;
      cacheService.holdTickets(ticketIds, memberId);

      // when
      cacheService.releaseTickets(ticketIds);

      // then
      Map<Long, Long> result = cacheService.getHoldMembers(ticketIds);
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("실제 시나리오: 예약 타임아웃으로 티켓 해제")
    void releaseOnBookingTimeout() {
      // given - 멤버가 5개 티켓 홀드
      List<Long> ticketIds =
          IntStream.rangeClosed(1, 5).mapToObj(Long::valueOf).collect(Collectors.toList());
      Long memberId = 100L;
      cacheService.holdTickets(ticketIds, memberId);

      // when - 결제 시간 초과로 티켓 해제
      cacheService.releaseTickets(ticketIds);

      // then - 다른 사용자가 예약 가능
      boolean unavailable = cacheService.hasUnavailableTickets(ticketIds, 200L);
      assertThat(unavailable).isFalse();
    }
  }

  @Nested
  @DisplayName("캐시 무효화 테스트")
  class InvalidationTest {

    @Test
    @DisplayName("특정 티켓들을 무효화한다")
    void invalidateSpecificTickets() {
      // given
      List<Long> ticketIds = List.of(1L, 2L, 3L);
      cacheService.holdTickets(ticketIds, 100L);

      // when
      cacheService.invalidate(List.of(1L, 2L));

      // then
      assertThat(cacheService.getHoldMember(1L)).isEmpty();
      assertThat(cacheService.getHoldMember(2L)).isEmpty();
      assertThat(cacheService.getHoldMember(3L)).isPresent(); // 3번은 그대로
    }

    @Test
    @DisplayName("전체 캐시를 무효화한다")
    void invalidateAll() {
      // given - 100개의 티켓 홀드
      List<Long> ticketIds =
          IntStream.rangeClosed(1, 100).mapToObj(Long::valueOf).collect(Collectors.toList());
      cacheService.holdTickets(ticketIds, 100L);

      // when
      cacheService.invalidateAll();

      // then
      assertThat(cacheService.size()).isEqualTo(0);
      Map<Long, Long> result = cacheService.getHoldMembers(ticketIds);
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("캐시 통계 테스트")
  class CacheStatsTest {

    @Test
    @DisplayName("캐시 통계를 조회한다")
    void getCacheStats() {
      // given
      cacheService.holdTicket(1L, 100L);
      cacheService.getHoldMember(1L); // hit
      cacheService.getHoldMember(999L); // miss

      // when
      CacheStats stats = cacheService.getStats();

      // then
      assertThat(stats.hitCount()).isGreaterThan(0);
      assertThat(stats.missCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("캐시 크기를 조회한다")
    void getCacheSize() {
      // given
      List<Long> ticketIds =
          IntStream.rangeClosed(1, 50).mapToObj(Long::valueOf).collect(Collectors.toList());
      cacheService.holdTickets(ticketIds, 100L);

      // when
      long size = cacheService.size();

      // then
      assertThat(size).isEqualTo(50);
    }
  }

  @Nested
  @DisplayName("TTL 및 만료 테스트")
  class ExpirationTest {

    @Test
    @DisplayName("짧은 TTL 설정 시 캐시가 만료된다")
    void expireAfterWrite() throws InterruptedException {
      // given - 1초 TTL로 캐시 생성
      CacheProperties shortTtlProperties =
          new CacheProperties(
              new CacheProperties.LocalCacheProperties(true, 10_000, Duration.ofSeconds(1), false),
              new CacheProperties.RedisCacheProperties(true, Duration.ofMinutes(10)),
              new CacheProperties.MultiLevelCacheProperties(
                  true, "cache:invalidation", true, true));
      LocalTicketCacheService shortTtlCache = new LocalTicketCacheService(shortTtlProperties);

      Long ticketId = 1L;
      Long memberId = 100L;
      shortTtlCache.holdTicket(ticketId, memberId);

      // when - 1.5초 대기
      Thread.sleep(1500);
      shortTtlCache.cleanUp();

      // then
      Optional<Long> result = shortTtlCache.getHoldMember(ticketId);
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("최대 크기 제한 테스트")
  class MaxSizeTest {

    @Test
    @DisplayName("최대 크기를 초과하면 오래된 엔트리가 제거된다")
    void evictWhenMaxSizeExceeded() {
      // given
      CacheProperties smallCacheProperties =
          new CacheProperties(
              new CacheProperties.LocalCacheProperties(
                  true,
                  10, // maxSize: 10
                  Duration.ofMinutes(10),
                  false),
              new CacheProperties.RedisCacheProperties(true, Duration.ofMinutes(10)),
              new CacheProperties.MultiLevelCacheProperties(
                  true, "cache:invalidation", true, true));
      LocalTicketCacheService smallCache = new LocalTicketCacheService(smallCacheProperties);

      // when
      for (long i = 1; i <= 20; i++) {
        smallCache.holdTicket(i, 100L);
      }

      smallCache.cleanUp();

      // then
      assertThat(smallCache.size()).isLessThanOrEqualTo(10);
    }
  }

  @Nested
  @DisplayName("실제 시나리오 통합 테스트")
  class RealWorldScenarioTest {

    @Test
    @DisplayName("시나리오: 콘서트 티켓 예약 플로우")
    void concertBookingFlow() {
      // given - 콘서트 좌석 (Section A: 1-50번, Section B: 51-100번)
      List<Long> sectionASeats =
          IntStream.rangeClosed(1, 50).mapToObj(Long::valueOf).collect(Collectors.toList());
      List<Long> sectionBSeats =
          IntStream.rangeClosed(51, 100).mapToObj(Long::valueOf).collect(Collectors.toList());

      // when - 멤버 1이 Section A 1-5번 선택
      Long member1 = 1L;
      List<Long> member1Tickets = List.of(1L, 2L, 3L, 4L, 5L);
      cacheService.holdTickets(member1Tickets, member1);

      // then - 멤버 1의 티켓은 홀드됨
      assertThat(cacheService.hasUnavailableTickets(member1Tickets, member1)).isFalse();

      // when - 멤버 2가 같은 티켓 선택 시도
      Long member2 = 2L;
      boolean member2CanBook = !cacheService.hasUnavailableTickets(member1Tickets, member2);

      // then - 멤버 2는 예약 불가
      assertThat(member2CanBook).isFalse();

      // when - 멤버 2가 다른 좌석 (6-10번) 선택
      List<Long> member2Tickets = List.of(6L, 7L, 8L, 9L, 10L);
      cacheService.holdTickets(member2Tickets, member2);

      // then - 멤버 2도 예약 가능
      assertThat(cacheService.hasUnavailableTickets(member2Tickets, member2)).isFalse();

      // when - 멤버 1이 결제 타임아웃으로 취소
      cacheService.releaseTickets(member1Tickets);

      // then - 멤버 3이 멤버 1의 좌석 예약 가능
      Long member3 = 3L;
      assertThat(cacheService.hasUnavailableTickets(member1Tickets, member3)).isFalse();
    }

    @Test
    @DisplayName("시나리오: 대량 티켓 예약 및 통계")
    void bulkBookingWithStats() {
      // given - 1000명의 멤버가 각각 티켓 예약
      int memberCount = 1000;

      // when
      for (int i = 1; i <= memberCount; i++) {
        cacheService.holdTicket((long) i, (long) i);
      }

      // then
      assertThat(cacheService.size()).isEqualTo(1000);

      CacheStats stats = cacheService.getStats();
      assertThat(stats.evictionCount()).isGreaterThanOrEqualTo(0);

      // 일부 조회 (캐시 히트)
      for (int i = 1; i <= 100; i++) {
        cacheService.getHoldMember((long) i);
      }

      CacheStats afterQueryStats = cacheService.getStats();
      assertThat(afterQueryStats.hitCount()).isGreaterThan(0);
    }
  }
}
