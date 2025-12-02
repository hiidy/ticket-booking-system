package com.seatwise.show.service.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import com.seatwise.annotation.EmbeddedRedisTest;
import com.seatwise.booking.entity.BookingRepository;
import com.seatwise.booking.exception.RecoverableBookingException;
import com.seatwise.core.BaseCode;
import com.seatwise.member.Member;
import com.seatwise.member.MemberRepository;
import com.seatwise.show.dto.request.ShowBookingRequest;
import com.seatwise.show.entity.Show;
import com.seatwise.show.entity.ShowType;
import com.seatwise.show.entity.Ticket;
import com.seatwise.show.repository.ShowRepository;
import com.seatwise.show.repository.TicketRepository;
import com.seatwise.venue.entity.Seat;
import com.seatwise.venue.entity.SeatRepository;
import com.seatwise.venue.entity.Venue;
import com.seatwise.venue.entity.VenueRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

@EmbeddedRedisTest
abstract class BookingStrategyIntegrationTestSupport {

  @Autowired protected TicketRepository ticketRepository;
  @Autowired protected ShowRepository showRepository;
  @Autowired protected SeatRepository seatRepository;
  @Autowired protected VenueRepository venueRepository;
  @Autowired protected MemberRepository memberRepository;
  @Autowired protected BookingRepository bookingRepository;
  @Autowired protected RedisTemplate<String, Object> redisTemplate;

  protected static final long SECTION_ID = 100L;

  @BeforeEach
  void setUp() {
    // DB 정리
    bookingRepository.deleteAll();
    ticketRepository.deleteAll();
    showRepository.deleteAll();
    seatRepository.deleteAll();
    venueRepository.deleteAll();
    memberRepository.deleteAll();

    // Redis 정리
    redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
  }

  protected ShowBookingRequest prepareBookingRequest() {
    Venue venue = venueRepository.save(new Venue("Test Venue", 100));
    Seat seat1 = seatRepository.save(new Seat("A", "1", venue));
    Seat seat2 = seatRepository.save(new Seat("A", "2", venue));

    Show show =
        showRepository.save(
            new Show(
                "Concert",
                "desc",
                ShowType.CONCERT,
                venue,
                LocalDate.now().plusDays(1),
                LocalTime.of(19, 0),
                LocalTime.of(21, 0)));

    Ticket ticket1 = ticketRepository.save(Ticket.createAvailable(show, seat1, SECTION_ID, 50000));
    Ticket ticket2 = ticketRepository.save(Ticket.createAvailable(show, seat2, SECTION_ID, 50000));

    Member member = memberRepository.save(new Member("tester", "test@example.com", "pw"));

    return new ShowBookingRequest(
        member.getId(), List.of(ticket1.getId(), ticket2.getId()), SECTION_ID);
  }

  protected List<Object> runConcurrent(Callable<String> task1, Callable<String> task2)
      throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(2);
    CountDownLatch start = new CountDownLatch(1);
    List<Callable<Object>> callables = List.of(wrap(start, task1), wrap(start, task2));

    List<Future<Object>> futures = new ArrayList<>();
    for (Callable<Object> callable : callables) {
      futures.add(executor.submit(callable));
    }

    start.countDown();

    List<Object> results = new ArrayList<>();
    for (Future<Object> future : futures) {
      results.add(future.get(3, TimeUnit.SECONDS));
    }

    executor.shutdownNow();
    return results;
  }

  private Callable<Object> wrap(CountDownLatch start, Callable<String> task) {
    return () -> {
      start.await();
      try {
        return task.call();
      } catch (Exception e) {
        return e;
      }
    };
  }

  protected void assertSingleSuccessAndSeatUnavailableFailure(List<Object> results) {
    long success = results.stream().filter(String.class::isInstance).count();
    long failures = results.stream().filter(RecoverableBookingException.class::isInstance).count();

    assertThat(success).isEqualTo(1);
    assertThat(failures).isGreaterThanOrEqualTo(1);

    results.stream()
        .filter(RecoverableBookingException.class::isInstance)
        .map(RecoverableBookingException.class::cast)
        .forEach(e -> assertThat(e.getBaseCode()).isEqualTo(BaseCode.SEAT_NOT_AVAILABLE));
  }

  protected UUID randomKey() {
    return UUID.randomUUID();
  }
}
